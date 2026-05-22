-- Petal Supabase Data API hardening
--
-- Petal uses Supabase for Postgres and Storage only. React must not call the
-- Supabase Data API directly; Spring Boot remains the application/runtime
-- authorization boundary.
--
-- This script enables RLS on app tables in the exposed public schema and
-- removes direct table/sequence/function grants from public API roles. It does
-- not create permissive anon/authenticated policies.

begin;

alter table public.cart_items enable row level security;
alter table public.delivery_addresses enable row level security;
alter table public.florists enable row level security;
alter table public.order_items enable row level security;
alter table public.orders enable row level security;
alter table public.product_mood_tags enable row level security;
alter table public.products enable row level security;
alter table public.saved_dates enable row level security;
alter table public.tracking_events enable row level security;
alter table public.users enable row level security;

revoke all privileges on table public.cart_items from anon, authenticated, public;
revoke all privileges on table public.delivery_addresses from anon, authenticated, public;
revoke all privileges on table public.florists from anon, authenticated, public;
revoke all privileges on table public.order_items from anon, authenticated, public;
revoke all privileges on table public.orders from anon, authenticated, public;
revoke all privileges on table public.product_mood_tags from anon, authenticated, public;
revoke all privileges on table public.products from anon, authenticated, public;
revoke all privileges on table public.saved_dates from anon, authenticated, public;
revoke all privileges on table public.tracking_events from anon, authenticated, public;
revoke all privileges on table public.users from anon, authenticated, public;

revoke all privileges on sequence public.cart_items_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.delivery_addresses_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.florists_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.order_items_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.orders_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.products_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.saved_dates_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.tracking_events_id_seq from anon, authenticated, public;
revoke all privileges on sequence public.users_id_seq from anon, authenticated, public;

revoke execute on all functions in schema public from anon, authenticated, public;

alter default privileges in schema public
  revoke all on tables from anon, authenticated, public;

alter default privileges in schema public
  revoke all on sequences from anon, authenticated, public;

alter default privileges in schema public
  revoke execute on functions from anon, authenticated, public;

commit;
