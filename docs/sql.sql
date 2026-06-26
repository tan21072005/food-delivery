-- ============================================================
--  FOOD DELIVERY APP — Supabase Schema v2 (NO RLS - DEV MODE)
--  Đã fix: thứ tự tạo object đúng, bỏ RLS, bỏ CHECK role
-- ============================================================

-- ============================================================
-- BƯỚC 0: DỌN SẠCH NẾU CẦN CHẠY LẠI
-- ============================================================
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- ============================================================
-- 1. ENUMS
-- ============================================================
CREATE TYPE user_role        AS ENUM ('admin', 'customer', 'restaurant', 'driver');
CREATE TYPE user_status      AS ENUM ('active', 'inactive', 'banned', 'pending_verify');
CREATE TYPE oauth_provider   AS ENUM ('google', 'facebook', 'apple');
CREATE TYPE device_platform  AS ENUM ('android', 'ios', 'web', 'windows');
CREATE TYPE verification_type AS ENUM ('email_verify', 'phone_verify', 'password_reset', 'login');
CREATE TYPE offer_type       AS ENUM ('discount', 'freeship');
CREATE TYPE discount_type    AS ENUM ('fixed', 'rate');
CREATE TYPE payment_mode     AS ENUM ('net_banking', 'COD', 'debit_card', 'credit_card');
CREATE TYPE payment_status   AS ENUM ('pending', 'paid', 'not_paid', 'refunded');
CREATE TYPE order_status     AS ENUM ('pending', 'confirmed', 'preparing', 'ready', 'on_the_way', 'delivered', 'cancelled');
CREATE TYPE media_type       AS ENUM ('image', 'video');
CREATE TYPE generic_status   AS ENUM ('active', 'inactive');

-- ============================================================
-- 2. GEOGRAPHY
-- ============================================================
CREATE TABLE countries (
    id           SERIAL PRIMARY KEY,
    country_name VARCHAR(100) NOT NULL,
    status       generic_status NOT NULL DEFAULT 'active',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE regions (
    id          SERIAL PRIMARY KEY,
    country_id  INT NOT NULL REFERENCES countries(id) ON DELETE CASCADE,
    region_name VARCHAR(100) NOT NULL,
    status      generic_status NOT NULL DEFAULT 'active',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cities (
    id         SERIAL PRIMARY KEY,
    country_id INT NOT NULL REFERENCES countries(id) ON DELETE CASCADE,
    region_id  INT REFERENCES regions(id) ON DELETE SET NULL,
    city_name  VARCHAR(100) NOT NULL,
    status     generic_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 3. USERS — tạo trước, function get_my_role() tạo SAU
-- ============================================================
CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    auth_uid          UUID UNIQUE,
    role              user_role NOT NULL DEFAULT 'customer',
    username          VARCHAR(50) UNIQUE NOT NULL,
    full_name         VARCHAR(150),
    email             VARCHAR(255) UNIQUE NOT NULL,
    password          VARCHAR(255),
    phone_number      VARCHAR(20) UNIQUE,
    avatar_url        TEXT,
    status            user_status NOT NULL DEFAULT 'pending_verify',
    phone_verified_at TIMESTAMPTZ,
    email_verified_at TIMESTAMPTZ,
    last_login_at     TIMESTAMPTZ,
    deleted_at        TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 4. HELPER FUNCTION — đặt SAU khi bảng users đã tồn tại
-- ============================================================
CREATE OR REPLACE FUNCTION get_my_role()
RETURNS TEXT LANGUAGE sql STABLE SECURITY DEFINER AS $$
    SELECT role::TEXT FROM users WHERE auth_uid = auth.uid();
$$;

-- ============================================================
-- 5. TRIGGER: Supabase auth → tự tạo row users
-- ============================================================
CREATE OR REPLACE FUNCTION handle_new_auth_user()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    INSERT INTO users (auth_uid, email, username, role)
    VALUES (
        NEW.id,
        NEW.email,
        SPLIT_PART(NEW.email, '@', 1),
        COALESCE(NEW.raw_user_meta_data->>'role', 'customer')::user_role
    )
    ON CONFLICT (auth_uid) DO NOTHING;
    RETURN NEW;
END;
$$;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION handle_new_auth_user();

-- ============================================================
-- 6. AUTH / VERIFICATION
-- ============================================================
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       TEXT NOT NULL UNIQUE,
    expired_at  TIMESTAMPTZ NOT NULL,
    is_revoked  BOOLEAN NOT NULL DEFAULT FALSE,
    device_info TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE verification_codes (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token             VARCHAR(10) NOT NULL,
    verification_type verification_type NOT NULL,
    attempts          SMALLINT NOT NULL DEFAULT 0,
    expires_at        TIMESTAMPTZ NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_oauth_providers (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider     oauth_provider NOT NULL,
    provider_uid VARCHAR(255) NOT NULL,
    access_token TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (provider, provider_uid)
);

CREATE TABLE user_devices (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token TEXT NOT NULL,
    platform     device_platform NOT NULL,
    device_type  VARCHAR(100),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, device_token)
);

-- ============================================================
-- 7. ADDRESS
-- ============================================================
CREATE TABLE addresses (
    id            BIGSERIAL PRIMARY KEY,
    unit_number   VARCHAR(20),
    street_number VARCHAR(20),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city          VARCHAR(100),
    region        VARCHAR(100),
    postal_code   VARCHAR(20),
    country_id    INT REFERENCES countries(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_addresses (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_id     BIGINT REFERENCES addresses(id) ON DELETE SET NULL,
    label          VARCHAR(50),
    address_detail TEXT NOT NULL,
    latitude       DECIMAL(10, 8),
    longitude      DECIMAL(11, 8),
    is_default     BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 8. PROFILE MỞ RỘNG
-- NOTE: Bỏ CONSTRAINT CHECK role vì gây lỗi khi insert trong dev
-- ============================================================
CREATE TABLE seller_profiles (
    user_id               BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    store_address         TEXT,
    store_description     TEXT,
    store_contract_number VARCHAR(20),
    store_created_at      DATE,
    store_updated_at      DATE
);

CREATE TABLE driver_profiles (
    user_id      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    img_url      TEXT,
    rating       DECIMAL(3, 2) NOT NULL DEFAULT 0,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    current_lat  DECIMAL(10, 8),
    current_lng  DECIMAL(11, 8),
    vehicle_no   VARCHAR(30),
    license_no   VARCHAR(50),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 9. RESTAURANT
-- ============================================================
CREATE TABLE restaurants (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    phone_number   VARCHAR(20),
    address_detail TEXT,
    locality       VARCHAR(150),
    latitude       DECIMAL(10, 8),
    longitude      DECIMAL(11, 8),
    logo_url       TEXT,
    cover_url      TEXT,
    avg_rating     DECIMAL(3, 2) NOT NULL DEFAULT 0,
    total_reviews  INT NOT NULL DEFAULT 0,
    total_orders   INT NOT NULL DEFAULT 0,
    is_open        BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE restaurant_timings (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    week_day      VARCHAR(10) NOT NULL,
    open_time     TIME NOT NULL,
    close_time    TIME NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (restaurant_id, week_day)
);

CREATE TABLE restaurant_media (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    media_name    TEXT NOT NULL,
    media_url     TEXT NOT NULL,
    media_type    media_type NOT NULL,
    status        generic_status NOT NULL DEFAULT 'active',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 10. MENU
-- ============================================================
CREATE TABLE menu_categories (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE menus (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id   BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
    item_name     VARCHAR(255) NOT NULL,
    description   TEXT,
    image_url     TEXT,
    price         DECIMAL(12, 2) NOT NULL DEFAULT 0,
    rating        DECIMAL(3, 2) NOT NULL DEFAULT 0,
    status        generic_status NOT NULL DEFAULT 'active',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE menu_variants (
    id           BIGSERIAL PRIMARY KEY,
    menu_id      BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    price        DECIMAL(12, 2) NOT NULL DEFAULT 0,
    is_default   BOOLEAN NOT NULL DEFAULT FALSE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 11. PRODUCT CATALOGUE
-- ============================================================
CREATE TABLE categories (
    id         BIGSERIAL PRIMARY KEY,
    cat_name   VARCHAR(100) NOT NULL,
    status     generic_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prod_name   VARCHAR(255) NOT NULL,
    description TEXT,
    price       DECIMAL(12, 2) NOT NULL DEFAULT 0,
    image_url   TEXT,
    status      generic_status NOT NULL DEFAULT 'active',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE category_products (
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (category_id, product_id)
);

-- ============================================================
-- 12. CART
-- ============================================================
CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    menu_id    BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    quantity   INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, menu_id)
);

-- ============================================================
-- 13. OFFERS / COUPONS
-- ============================================================
CREATE TABLE offers (
    id                  BIGSERIAL PRIMARY KEY,
    coupon_code         VARCHAR(50) UNIQUE NOT NULL,
    offer_type          offer_type NOT NULL,
    discount_type       discount_type,
    discount_value      DECIMAL(12, 2) NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(12, 2),
    min_order_amount    DECIMAL(12, 2) NOT NULL DEFAULT 0,
    max_uses            INT,
    start_date          TIMESTAMPTZ NOT NULL,
    end_date            TIMESTAMPTZ NOT NULL,
    description         TEXT,
    status              generic_status NOT NULL DEFAULT 'active',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (end_date > start_date)
);

-- ============================================================
-- 14. ORDERS
-- ============================================================
CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id),
    restaurant_id    BIGINT NOT NULL REFERENCES restaurants(id),
    driver_id        BIGINT,
    status           order_status NOT NULL DEFAULT 'pending',
    total_amount     DECIMAL(12, 2) NOT NULL DEFAULT 0,
    delivery_fee     DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ship_discount    DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount  DECIMAL(12, 2) NOT NULL DEFAULT 0,
    net_amount       DECIMAL(12, 2) NOT NULL DEFAULT 0,
    payment_mode     payment_mode,
    payment_status   payment_status NOT NULL DEFAULT 'pending',
    delivery_address TEXT,
    note             TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id    BIGINT NOT NULL REFERENCES menus(id),
    variant_id BIGINT REFERENCES menu_variants(id),
    quantity   INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal   DECIMAL(12, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED
);

CREATE TABLE order_applied_offers (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    offer_id        BIGINT NOT NULL REFERENCES offers(id),
    offer_type      offer_type NOT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (order_id, offer_id)
);

CREATE TABLE user_offer_usages (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    order_id        BIGINT NOT NULL REFERENCES orders(id),
    offer_id        BIGINT NOT NULL REFERENCES offers(id),
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    used_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, order_id, offer_id)
);

-- ============================================================
-- 15. PAYMENT
-- ============================================================
CREATE TABLE payments (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    gateway        VARCHAR(100),
    gateway_txn_id VARCHAR(255) UNIQUE,
    amount         DECIMAL(12, 2) NOT NULL,
    pay_method     VARCHAR(100),
    status         payment_status NOT NULL DEFAULT 'pending',
    payment_date   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 16. FK: orders → driver_profiles (thêm sau khi cả 2 bảng đã tồn tại)
-- ============================================================
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_driver
    FOREIGN KEY (driver_id) REFERENCES driver_profiles(user_id) ON DELETE SET NULL;

-- ============================================================
-- 17. INDEXES
-- ============================================================
CREATE INDEX idx_users_role       ON users(role);
CREATE INDEX idx_users_email      ON users(email);
CREATE INDEX idx_users_phone      ON users(phone_number);
CREATE INDEX idx_users_status     ON users(status);
CREATE INDEX idx_users_auth_uid   ON users(auth_uid);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_verification_user   ON verification_codes(user_id);

CREATE INDEX idx_restaurants_user     ON restaurants(user_id);
CREATE INDEX idx_restaurants_location ON restaurants(latitude, longitude);
CREATE INDEX idx_restaurants_open     ON restaurants(is_open);

CREATE INDEX idx_menus_restaurant ON menus(restaurant_id);
CREATE INDEX idx_menus_category   ON menus(category_id);
CREATE INDEX idx_menus_status     ON menus(status);

CREATE INDEX idx_orders_user       ON orders(user_id);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX idx_orders_driver     ON orders(driver_id);
CREATE INDEX idx_orders_status     ON orders(status);
CREATE INDEX idx_orders_created    ON orders(created_at DESC);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_payments_order    ON payments(order_id);

CREATE INDEX idx_offers_code         ON offers(coupon_code);
CREATE INDEX idx_offers_status_dates ON offers(status, start_date, end_date);

CREATE INDEX idx_user_addresses_user ON user_addresses(user_id);
CREATE INDEX idx_carts_user          ON carts(user_id);
CREATE INDEX idx_driver_available    ON driver_profiles(is_available);

-- ============================================================
-- 18. UPDATED_AT TRIGGER
-- ============================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

DO $$
DECLARE tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY[
        'users','countries','regions','cities',
        'addresses','user_addresses',
        'verification_codes',
        'driver_profiles',
        'restaurants','restaurant_timings',
        'menu_categories','menus','menu_variants',
        'categories','products',
        'carts','offers','orders','payments'
    ]
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%I_updated_at
             BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION set_updated_at();',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- ============================================================
-- DONE — Paste toàn bộ vào Supabase SQL Editor → Run without RLS
-- Khi làm xong frontend, chạy thêm file enable_rls.sql
-- ============================================================