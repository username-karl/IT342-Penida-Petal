-- Petal Supabase Storage buckets.
-- Run in the Supabase SQL editor for the target project.
-- Spring Boot uploads with a backend-only service role key. Do not expose that key to React.

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
  ('florist-logos', 'florist-logos', true, 5242880, array['image/png', 'image/jpeg', 'image/jpg', 'image/webp']),
  ('order-photos', 'order-photos', false, 5242880, array['image/png', 'image/jpeg', 'image/jpg', 'image/webp'])
on conflict (id) do update
set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

-- No anon/authenticated storage upload policies are required.
-- order-photos stays private; the backend returns short-lived signed URLs only
-- after Spring Security and order ownership checks have passed.
