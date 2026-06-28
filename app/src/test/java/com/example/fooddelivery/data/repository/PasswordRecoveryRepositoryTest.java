package com.example.fooddelivery.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.remote.apis.PasswordRecoveryApiService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PasswordRecoveryRepositoryTest {
    private MockWebServer server;
    private PasswordRecoveryRepository repository;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        PasswordRecoveryApiService service = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PasswordRecoveryApiService.class);
        repository = new PasswordRecoveryRepository(service, "test-anon-key");
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void sendsRecoveryEmailToCorrectEndpoint() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        AwaitingCallback<Void> callback = new AwaitingCallback<>();

        repository.sendCode("user@example.com", callback);

        RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/auth/v1/recover", request.getPath());
        assertEquals("Bearer test-anon-key", request.getHeader("Authorization"));
        assertTrue(request.getBody().readUtf8().contains("\"email\":\"user@example.com\""));
        callback.await();
        assertEquals(1, callback.successCount);
    }

    @Test
    public void verifiesRecoveryOtpAndReturnsAccessToken() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"recovery-token\"}"));
        AwaitingCallback<String> callback = new AwaitingCallback<>();

        repository.verifyOtp("user@example.com", "123456", callback);

        RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/auth/v1/verify", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"token\":\"123456\""));
        assertTrue(body.contains("\"type\":\"recovery\""));
        callback.await();
        assertEquals("recovery-token", callback.value.get());
    }

    @Test
    public void updatesPasswordWithRecoveryBearer() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));
        AwaitingCallback<Void> callback = new AwaitingCallback<>();

        repository.updatePassword("recovery-token", "NewPassword1!", callback);

        RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/auth/v1/user", request.getPath());
        assertEquals("Bearer recovery-token", request.getHeader("Authorization"));
        assertTrue(request.getBody().readUtf8().contains("\"password\":\"NewPassword1!\""));
        callback.await();
        assertEquals(1, callback.successCount);
    }

    @Test
    public void mapsOtpAndRateLimitErrors() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(403));
        AwaitingCallback<String> invalidOtp = new AwaitingCallback<>();
        repository.verifyOtp("user@example.com", "000000", invalidOtp);
        invalidOtp.await();
        assertEquals("Mã xác minh không đúng hoặc đã hết hạn",
                invalidOtp.error.get().userMessage());

        server.enqueue(new MockResponse().setResponseCode(429));
        AwaitingCallback<Void> rateLimited = new AwaitingCallback<>();
        repository.sendCode("user@example.com", rateLimited);
        rateLimited.await();
        assertEquals("Bạn thao tác quá nhanh. Vui lòng thử lại sau",
                rateLimited.error.get().userMessage());
    }

    @Test
    public void rejectsVerifyResponseWithoutAccessToken() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));
        AwaitingCallback<String> callback = new AwaitingCallback<>();

        repository.verifyOtp("user@example.com", "123456", callback);

        callback.await();
        assertNotNull(callback.error.get());
    }

    @Test
    public void parsesKnownSupabaseOtpErrorsAndHidesUnknownDetails() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"code\":\"otp_expired\",\"msg\":\"Token has expired\"}"));
        AwaitingCallback<String> expired = new AwaitingCallback<>();
        repository.verifyOtp("user@example.com", "123456", expired);
        expired.await();
        assertEquals("Mã xác minh không đúng hoặc đã hết hạn",
                expired.error.get().userMessage());

        server.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"sensitive internal server detail\"}"));
        AwaitingCallback<Void> unknown = new AwaitingCallback<>();
        repository.sendCode("user@example.com", unknown);
        unknown.await();
        assertEquals("Không thể xử lý yêu cầu. Vui lòng thử lại",
                unknown.error.get().userMessage());
    }

    private static final class AwaitingCallback<T>
            implements PasswordRecoveryRepository.ResultCallback<T> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<T> value = new AtomicReference<>();
        private final AtomicReference<PasswordRecoveryRepository.RecoveryError> error =
                new AtomicReference<>();
        private int successCount;

        @Override
        public void onSuccess(T value) {
            this.value.set(value);
            successCount++;
            latch.countDown();
        }

        @Override
        public void onError(PasswordRecoveryRepository.RecoveryError error) {
            this.error.set(error);
            latch.countDown();
        }

        void await() throws InterruptedException {
            assertTrue("Callback timed out", latch.await(2, TimeUnit.SECONDS));
        }
    }
}
