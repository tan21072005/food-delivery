-- Function to fetch all data needed for Home Screen in a single API call
CREATE OR REPLACE FUNCTION get_home_data()
RETURNS json AS $$
DECLARE
  result json;
BEGIN
  SELECT json_build_object(
    'categories', COALESCE((
        SELECT json_agg(c) FROM (
            SELECT id, cat_name, icon_url, status 
            FROM categories 
            WHERE status = 'active'
        ) c
    ), '[]'::json),
    
    'top_selling', COALESCE((
        SELECT json_agg(m) FROM (
            SELECT id, restaurant_id, category_id, item_name, description, image_url, price, rating, status 
            FROM menus 
            WHERE status = 'active' 
            ORDER BY rating DESC 
            LIMIT 10
        ) m
    ), '[]'::json),
    
    'all_foods', COALESCE((
        SELECT json_agg(m) FROM (
            SELECT id, restaurant_id, category_id, item_name, description, image_url, price, rating, status 
            FROM menus 
            WHERE status = 'active'
        ) m
    ), '[]'::json)
  ) INTO result;
  
  RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
