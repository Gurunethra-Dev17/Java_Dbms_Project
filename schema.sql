-- Run in Supabase: SQL Editor -> New query -> paste -> Run
create table if not exists shows (
  id int generated always as identity primary key,
  movie text not null,
  show_time text not null,
  hue int not null default 35
);

create table if not exists bookings (
  id bigint generated always as identity (start with 1001) primary key,
  show_id int not null references shows(id),
  seats text not null,                       -- "C3,C4"
  original_amount numeric(10,2) not null,
  discount numeric(10,2) not null default 0,
  amount numeric(10,2) not null,
  refund numeric(10,2) not null default 0,
  coupon text not null default '',
  payment_method text not null,
  payment_status text not null default 'PAID',
  status text not null default 'BOOKED' check (status in ('BOOKED','CANCELLED')),
  created_at timestamptz not null default now()
);

-- PK on (show_id, seat_code) = DB-level double-booking protection
create table if not exists booked_seats (
  show_id int not null references shows(id),
  seat_code text not null check (seat_code ~ '^[A-E][1-8]$'),
  booking_id bigint not null references bookings(id) on delete cascade,
  primary key (show_id, seat_code)
);

-- Lock tables from the public anon API; the Java backend connects as the
-- postgres role, which bypasses RLS.
alter table shows enable row level security;
alter table bookings enable row level security;
alter table booked_seats enable row level security;

insert into shows (movie, show_time, hue) values
  ('Interstellar','7:30 PM',35),
  ('Inception','4:00 PM',200),
  ('Avatar','10:00 AM',165);
