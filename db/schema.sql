-- ============================================================================
-- PostgreSQL Complete Schema Dump (DDL only, no data)
-- Source properties : application-sw-indev-tr.properties
-- Database          : swviergedb
-- Server version    : PostgreSQL 17.9 on x86_64-pc-linux-gnu, compiled by x86_64-pc-linux-gnu-gcc (GCC) 12.4.0, 64-bit
-- Generated at      : 2026-11-23T09:20:38.8527587-06:00
-- ============================================================================

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- ============================================================================
-- 1. SCHEMAS
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS public;
CREATE SCHEMA IF NOT EXISTS corepay_schema;

SET search_path = public;

-- ============================================================================
-- 2. SEQUENCES
-- ============================================================================

CREATE SEQUENCE public.client_activity_data_client_activity_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.comm_alarms_data_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.log_meta_data_log_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.reverses_data_reverse_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.system_stats_system_stats_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.system_trace_audit_number_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CACHE 100
    CYCLE;

CREATE SEQUENCE public.transaction_data_transaction_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.transactions_meta_data_transactions_meta_data_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.vault_data_vault_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

-- ============================================================================
-- 3. TABLES
-- ============================================================================

CREATE TABLE public.bines (
    bin character varying(8) NOT NULL,
    brand character varying(100),
    bank character varying(255),
    type character varying(50),
    category character varying(100),
    country character varying(100),
    latitude numeric(10,6),
    longitud numeric(10,6)
);

CREATE TABLE public.client_activity_data (
    client_activity_id bigint DEFAULT nextval('client_activity_data_client_activity_id_seq'::regclass) NOT NULL,
    account_locked boolean NOT NULL,
    account_suspicious boolean NOT NULL,
    client_email character varying(255),
    client_geolocation character varying(255),
    clientip character varying(255),
    client_user_name character varying(255),
    clonned_requests integer NOT NULL,
    creation_date_time timestamp without time zone,
    device_model character varying(255),
    failed_login_attempts integer NOT NULL,
    frequent_location character varying(255),
    is_blocked boolean NOT NULL,
    last_login_date_time timestamp without time zone,
    last_modification_date_time timestamp without time zone,
    last_transaction_date_time timestamp without time zone,
    number_of_devices integer NOT NULL,
    number_of_rejected_transactions integer NOT NULL,
    number_of_transactions integer NOT NULL,
    reason_for_blocking character varying(255),
    registration_date_time timestamp without time zone,
    suspicious_activity_detected boolean NOT NULL,
    terminal_serial_number character varying(255),
    total_amount_spent double precision NOT NULL
);

CREATE TABLE public.comm_alarms_data (
    id bigint DEFAULT nextval('comm_alarms_data_id_seq'::regclass) NOT NULL,
    creation_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    server_name character varying(255) NOT NULL,
    alarm_cause character varying(100) NOT NULL,
    error_description text
);

CREATE TABLE public.currency_symbols (
    currency_code text NOT NULL,
    numeric_code integer NOT NULL,
    symbol text NOT NULL
);

CREATE TABLE public.daily_error_summary (
    summary_date date DEFAULT CURRENT_DATE NOT NULL,
    host_type character varying(255) DEFAULT 'Undefined'::character varying NOT NULL,
    issuer_bank character varying(255) DEFAULT 'Undefined'::character varying NOT NULL,
    response_code character varying(255) DEFAULT 'NA'::character varying NOT NULL,
    error_code character varying(255) DEFAULT 'NA'::character varying NOT NULL,
    error_message character varying(1000) DEFAULT ''::character varying NOT NULL,
    error_count bigint DEFAULT 0 NOT NULL,
    id character varying(36) DEFAULT md5((random())::text) NOT NULL
);

CREATE TABLE public.daily_error_summarydbdata (
    id character varying(255) NOT NULL,
    error_code character varying(255),
    error_count bigint NOT NULL,
    error_message character varying(255),
    host_type character varying(255),
    issuer_bank character varying(255),
    response_code character varying(255),
    summary_date date
);

CREATE TABLE public.daily_transaction_summary (
    summary_date date DEFAULT CURRENT_DATE NOT NULL,
    host_type character varying(255) DEFAULT 'Undefined'::character varying NOT NULL,
    issuer_bank character varying(255) DEFAULT 'Undefined'::character varying NOT NULL,
    transaction_currency_code text DEFAULT 'N/A'::text NOT NULL,
    total_transactions bigint DEFAULT 0 NOT NULL,
    total_amount numeric(19,2) DEFAULT 0.00 NOT NULL,
    id character varying(36) DEFAULT md5((random())::text) NOT NULL
);

CREATE TABLE public.daily_transaction_summarydbdata (
    id character varying(255) NOT NULL,
    host_type character varying(255),
    issuer_bank character varying(255),
    summary_date date,
    total_amount numeric(19,2),
    total_transactions bigint NOT NULL,
    transaction_currency_code character varying(255)
);

CREATE TABLE public.log_meta_data (
    log_id bigint DEFAULT nextval('log_meta_data_log_id_seq'::regclass) NOT NULL,
    bank character varying(255),
    client_email character varying(255),
    client_geolocation character varying(255),
    clientip character varying(255),
    client_user_name character varying(255),
    creation_date_time timestamp without time zone,
    device_model character varying(255),
    error_code character varying(255),
    error_code_number integer NOT NULL,
    error_message character varying(1000),
    host_type character varying(255),
    last_modification_date_time timestamp without time zone,
    merchant_type character varying(255),
    module_id character varying(255),
    operation_duration real NOT NULL,
    operation_id character varying(255),
    point_of_service_entry_mode character varying(255),
    response_code character varying(255),
    rrn character varying(255),
    server_name character varying(255),
    terminal_serial_number character varying(255),
    transaction_amount character varying(255),
    transaction_type character varying(255),
    uuid bytea NOT NULL,
    time_zone character varying(100) DEFAULT 'UTC'::character varying NOT NULL,
    creation_timestamp timestamp without time zone DEFAULT timezone('UTC'::text, CURRENT_TIMESTAMP) NOT NULL,
    card_type character varying DEFAULT 'Undefined'::character varying NOT NULL,
    card_bin character varying DEFAULT ''::character varying NOT NULL,
    card_number character varying DEFAULT ''::character varying NOT NULL,
    affiliation character varying(100) DEFAULT ''::character varying NOT NULL,
    merchant_name character varying(200) DEFAULT ''::character varying NOT NULL,
    merchant_type_code character varying(70) DEFAULT ''::character varying NOT NULL,
    merchant_address character varying(200) DEFAULT ''::character varying NOT NULL,
    bank_name character varying(100) DEFAULT ''::character varying NOT NULL,
    cpu_usage_percentage numeric(19,2),
    network_usage_percentage numeric(19,2),
    ram_total numeric(19,2),
    ram_usage_percentage numeric(19,2),
    ram_used numeric(19,2),
    storage_total numeric(19,2),
    storage_usage_percentage numeric(19,2),
    storage_used numeric(19,2)
);

CREATE TABLE public.reverses_data (
    reverse_id bigint DEFAULT nextval('reverses_data_reverse_id_seq'::regclass) NOT NULL,
    authorization_id_response character varying(255),
    card_type character varying(255),
    emv_data text NOT NULL,
    host_type character varying(255),
    isoascii text NOT NULL,
    last_modification_date_time timestamp without time zone,
    merchant_type character varying(255),
    original_data_elements text NOT NULL,
    payment_plan_type character varying(255),
    point_of_service_entry_mode character varying(255),
    response_code character varying(255),
    rrn character varying(255),
    terminal_serial_number character varying(255),
    transaction_amount character varying(255),
    transaction_datajson text NOT NULL,
    transaction_direction character varying(255),
    transaction_id bigint NOT NULL,
    transaction_message_type character varying(255),
    transaction_status character varying(255),
    transaction_type character varying(255),
    iso_reverse_request text,
    result_message text,
    iso_reverse_response text,
    must_be_reversed boolean DEFAULT false NOT NULL,
    sender text,
    reverse_status text DEFAULT 'Created'::text
);

CREATE TABLE public.system_stats (
    system_stats_id integer DEFAULT nextval('system_stats_system_stats_id_seq'::regclass) NOT NULL,
    ram_total bigint NOT NULL,
    ram_used bigint NOT NULL,
    ram_usage_percentage numeric(5,2) NOT NULL,
    storage_total bigint NOT NULL,
    storage_used bigint NOT NULL,
    storage_usage_percentage numeric(5,2) NOT NULL,
    network_usage_percentage numeric(5,2) NOT NULL,
    cpu_usage_percentage numeric(5,2) NOT NULL,
    connection_count integer NOT NULL,
    timestamp timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.transaction_data (
    transaction_id bigint DEFAULT nextval('transaction_data_transaction_id_seq'::regclass) NOT NULL,
    authorization_id_response character varying(255),
    bank character varying(255),
    card_type character varying(255),
    client_email character varying(255),
    clientip character varying(255),
    client_location character varying(255),
    client_user_name character varying(255),
    creation_date_time timestamp without time zone,
    db_kcv text NOT NULL,
    device_model character varying(255),
    emv_data bytea NOT NULL,
    host_type character varying(255),
    isoasciirequest bytea NOT NULL,
    isoasciiresponse bytea NOT NULL,
    key_serial_number text NOT NULL,
    last_modification_date_time timestamp without time zone,
    merchant_type character varying(255),
    meta_data bytea NOT NULL,
    original_transaction_id bigint NOT NULL,
    payment_plan_type character varying(255),
    point_of_service_entry_mode character varying(255),
    response_code character varying(255),
    rrn character varying(255),
    server_name text NOT NULL,
    terminal_serial_number character varying(255),
    transaction_amount character varying(255),
    transaction_direction character varying(255),
    transaction_message_type character varying(255),
    transaction_status character varying(255),
    transaction_type character varying(255),
    uuid bytea NOT NULL,
    operation_id character varying DEFAULT 'Undefined'::character varying NOT NULL,
    error_code character varying(255) DEFAULT 'Approved'::character varying NOT NULL,
    error_code_number integer DEFAULT 0 NOT NULL,
    error_message character varying(1000) DEFAULT ''::character varying NOT NULL,
    time_zone character varying(100) DEFAULT 'UTC'::character varying NOT NULL,
    creation_timestamp timestamp without time zone DEFAULT timezone('UTC'::text, CURRENT_TIMESTAMP) NOT NULL,
    fiid character varying DEFAULT ''::character varying,
    affiliation character varying DEFAULT ''::character varying,
    merchant_name character varying DEFAULT ''::character varying,
    merchant_type_code character varying DEFAULT ''::character varying,
    merchant_address character varying DEFAULT ''::character varying,
    bank_name character varying DEFAULT ''::character varying,
    card_bin character varying DEFAULT ''::character varying NOT NULL,
    card_number character varying DEFAULT ''::character varying NOT NULL,
    transaction_amount_bd numeric(10,2),
    transaction_currency_code_number integer DEFAULT 484 NOT NULL,
    transaction_currency_code text,
    transaction_currency_symbol text,
    cpu_usage_percentage numeric(19,2),
    network_usage_percentage numeric(19,2),
    ram_total numeric(19,2),
    ram_usage_percentage numeric(19,2),
    ram_used numeric(19,2),
    storage_total numeric(19,2),
    storage_usage_percentage numeric(19,2),
    storage_used numeric(19,2),
    client_id character varying(255),
    transaction_id_mpp character varying(100) DEFAULT ''::character varying,
    system_trace_audit_number character varying(20) DEFAULT ''::character varying,
    device_type character varying(255) DEFAULT 'POS'::character varying,
    ram_total_pm numeric(17,2) DEFAULT 0 NOT NULL,
    ram_used_pm numeric(17,2) DEFAULT 0 NOT NULL,
    ram_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    storage_total_pm numeric(27,2) DEFAULT 0 NOT NULL,
    storage_used_pm numeric(27,2) DEFAULT 0 NOT NULL,
    storage_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    network_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    cpu_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    connection_count_pm integer DEFAULT 0 NOT NULL,
    operation_duration double precision DEFAULT 0 NOT NULL
);

CREATE TABLE public.transactions_meta_data (
    transactions_meta_data_id bigint DEFAULT nextval('transactions_meta_data_transactions_meta_data_id_seq'::regclass) NOT NULL,
    date_time_initial timestamp with time zone NOT NULL,
    date_time_end timestamp with time zone NOT NULL,
    report_time_period text NOT NULL,
    host_type character varying(255),
    server_name text NOT NULL,
    time_zone character varying(100) DEFAULT 'UTC'::character varying NOT NULL,
    terminal_serial_number character varying(255),
    merchant_type character varying(255),
    merchant_type_code character varying(255),
    transaction_type character varying(255),
    total_transaction_approved_amount numeric(29,2) DEFAULT 0 NOT NULL,
    total_transaction_approved_count bigint DEFAULT 0 NOT NULL,
    total_transaction_rejected_amount numeric(29,2) DEFAULT 0 NOT NULL,
    total_transaction_rejected_count bigint DEFAULT 0 NOT NULL,
    creation_date_time timestamp with time zone DEFAULT now(),
    last_modification_date_time timestamp with time zone DEFAULT now(),
    report_type text DEFAULT 'Terminal'::text NOT NULL,
    transaction_currency_code_number integer DEFAULT 484 NOT NULL,
    transaction_currency_code text,
    transaction_currency_symbol text,
    total_txns_commerr_count bigint DEFAULT 0 NOT NULL,
    last_update_pm_date_time timestamp with time zone DEFAULT now(),
    total_txns_rejected_pm bigint DEFAULT 0 NOT NULL,
    total_txns_approved_pm bigint DEFAULT 0 NOT NULL,
    total_transaction_commerr_pm bigint DEFAULT 0 NOT NULL,
    ram_total_pm bigint DEFAULT 0 NOT NULL,
    ram_used_pm bigint DEFAULT 0 NOT NULL,
    ram_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    storage_total_pm bigint DEFAULT 0 NOT NULL,
    storage_used_pm bigint DEFAULT 0 NOT NULL,
    storage_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    network_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    cpu_usage_percentage_pm numeric(5,2) DEFAULT 0.00 NOT NULL,
    connection_count_pm integer DEFAULT 0 NOT NULL
);

CREATE TABLE public.vault_data (
    vault_id bigint DEFAULT nextval('vault_data_vault_id_seq'::regclass) NOT NULL,
    creation_timestamp timestamp without time zone NOT NULL,
    modification_timestamp timestamp without time zone,
    vault_data character varying(255) NOT NULL,
    vault_key character varying(255) NOT NULL
);

-- ============================================================================
-- 4. SEQUENCE OWNERSHIP
-- ============================================================================

ALTER SEQUENCE public.client_activity_data_client_activity_id_seq OWNED BY public.client_activity_data.client_activity_id;
ALTER SEQUENCE public.comm_alarms_data_id_seq OWNED BY public.comm_alarms_data.id;
ALTER SEQUENCE public.log_meta_data_log_id_seq OWNED BY public.log_meta_data.log_id;
ALTER SEQUENCE public.reverses_data_reverse_id_seq OWNED BY public.reverses_data.reverse_id;
ALTER SEQUENCE public.system_stats_system_stats_id_seq OWNED BY public.system_stats.system_stats_id;
ALTER SEQUENCE public.transaction_data_transaction_id_seq OWNED BY public.transaction_data.transaction_id;
ALTER SEQUENCE public.transactions_meta_data_transactions_meta_data_id_seq OWNED BY public.transactions_meta_data.transactions_meta_data_id;
ALTER SEQUENCE public.vault_data_vault_id_seq OWNED BY public.vault_data.vault_id;

-- ============================================================================
-- 5. CONSTRAINTS (PRIMARY KEYS, UNIQUE, CHECK, FOREIGN KEYS)
-- ============================================================================

ALTER TABLE ONLY public.bines
    ADD CONSTRAINT bines_pkey PRIMARY KEY (bin);

ALTER TABLE ONLY public.client_activity_data
    ADD CONSTRAINT client_activity_data_pkey PRIMARY KEY (client_activity_id);

ALTER TABLE ONLY public.comm_alarms_data
    ADD CONSTRAINT pk_comm_alarms_data PRIMARY KEY (id);

ALTER TABLE ONLY public.currency_symbols
    ADD CONSTRAINT currency_symbols_pkey PRIMARY KEY (currency_code);

ALTER TABLE ONLY public.daily_error_summary
    ADD CONSTRAINT pk_daily_error_summary PRIMARY KEY (id);

ALTER TABLE ONLY public.daily_error_summarydbdata
    ADD CONSTRAINT daily_error_summarydbdata_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.daily_transaction_summary
    ADD CONSTRAINT pk_daily_transaction_summary PRIMARY KEY (id);

ALTER TABLE ONLY public.daily_transaction_summarydbdata
    ADD CONSTRAINT daily_transaction_summarydbdata_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.log_meta_data
    ADD CONSTRAINT log_meta_data_pkey PRIMARY KEY (log_id);

ALTER TABLE ONLY public.reverses_data
    ADD CONSTRAINT reverses_data_pkey PRIMARY KEY (reverse_id);

ALTER TABLE ONLY public.system_stats
    ADD CONSTRAINT system_stats_pkey PRIMARY KEY (system_stats_id);

ALTER TABLE ONLY public.transaction_data
    ADD CONSTRAINT transaction_data_pkey PRIMARY KEY (transaction_id);

ALTER TABLE ONLY public.transactions_meta_data
    ADD CONSTRAINT transactions_meta_data_pkey PRIMARY KEY (transactions_meta_data_id);

ALTER TABLE ONLY public.vault_data
    ADD CONSTRAINT vault_data_pkey PRIMARY KEY (vault_id);

ALTER TABLE ONLY public.client_activity_data
    ADD CONSTRAINT unique_client_email_terminal_serial_number UNIQUE (client_email, terminal_serial_number);

ALTER TABLE ONLY public.log_meta_data
    ADD CONSTRAINT unique_uuid UNIQUE (uuid);

ALTER TABLE ONLY public.vault_data
    ADD CONSTRAINT uk_mu1cw1soy4xgok3etcv7yc57r UNIQUE (vault_key);

-- ============================================================================
-- 6. INDEXES
-- ============================================================================

CREATE INDEX idx_log_creation_date_time ON public.log_meta_data USING btree (creation_date_time);
CREATE INDEX idx_log_operation_id ON public.log_meta_data USING btree (operation_id);
CREATE INDEX idx_log_rrn_terminal_host ON public.log_meta_data USING btree (rrn, terminal_serial_number, host_type);
CREATE INDEX idx_log_uuid ON public.log_meta_data USING btree (uuid);
CREATE INDEX idx_rrn_terminal_host ON public.transaction_data USING btree (rrn, terminal_serial_number, host_type);
CREATE INDEX idx_transaction_id ON public.transaction_data USING btree (transaction_id);
CREATE INDEX idx_transactions_meta_data_end ON public.transactions_meta_data USING btree (date_time_end);
CREATE INDEX idx_transactions_meta_data_initial ON public.transactions_meta_data USING btree (date_time_initial);

-- ============================================================================
-- 7. FUNCTIONS & PROCEDURES
-- ============================================================================

CREATE OR REPLACE FUNCTION public.delete_reverse(p_transaction_id bigint DEFAULT NULL::bigint)
 RETURNS SETOF reverses_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    UPDATE reverses_data
    SET reverse_status = 'Approved' 
    WHERE transaction_id = p_transaction_id
    RETURNING *;
END; 
$function$;

CREATE OR REPLACE FUNCTION public.fn_update_daily_summaries()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
    ---------------------------------------------------------------------------
    -- FASE 0: AUTO-LIMPIEZA SIN CUELLOS DE BOTELLA (Advisory Lock)
    ---------------------------------------------------------------------------
    -- Verificamos si hay datos del día anterior o más viejos
    IF EXISTS (
        SELECT 1 FROM public.daily_transaction_summary
        WHERE summary_date < CURRENT_DATE
        LIMIT 1
    ) THEN
        -- Intentamos obtener el bloqueo a nivel de transacción (ID: 9999)
        -- Si otro proceso ya lo tiene, devuelve FALSE y pasa de largo sin bloquearse
        IF pg_try_advisory_xact_lock(9999) THEN
            DELETE FROM public.daily_error_summary WHERE summary_date < CURRENT_DATE;
            DELETE FROM public.daily_transaction_summary WHERE summary_date < CURRENT_DATE;
        END IF;
    END IF;

    ---------------------------------------------------------------------------
    -- FASE PRINCIPAL: VALIDACIÓN ÚNICA PARA ERRORES Y MONTOS
    ---------------------------------------------------------------------------
    --  ADVERTENCIA: Si esto es un Trigger de UPDATE, considera agregar:
    -- AND (OLD.response_code IS NULL OR OLD.response_code = '')
    -- para evitar contar la misma transacción dos veces en updates futuros.
    IF (NEW.response_code IS NOT NULL AND NEW.response_code != '') THEN

        -- 1. GRABAR EN TABLA DE ERRORES (Agrupado)
        INSERT INTO public.daily_error_summary (
            summary_date,
            host_type,
            issuer_bank,
            response_code,
            error_code,
            error_message,
            error_count
        ) VALUES (
            CURRENT_DATE,
            COALESCE(NEW.host_type, 'Undefined'),
            COALESCE(NEW.bank_name, 'Undefined'),
            COALESCE(NEW.response_code, 'N/A'),
            COALESCE(NEW.error_code, 'N/A'),
            COALESCE(NEW.error_message, 'Sin mensaje'),
            1
        )
        ON CONFLICT (summary_date, host_type, issuer_bank, response_code, error_code, error_message)
        DO UPDATE SET error_count = public.daily_error_summary.error_count + 1;

        -- 2. GRABAR EN TABLA DE TRANSACCIONES Y MONTOS (Agrupado)
        INSERT INTO public.daily_transaction_summary (
            summary_date,
            host_type,                   -- SE AGREGA EL HOST
            issuer_bank,                 -- SE AGREGA EL BANCO
            transaction_currency_code,
            total_transactions,
            total_amount
        ) VALUES (
            CURRENT_DATE,
            COALESCE(NEW.host_type, 'Undefined'),
            COALESCE(NEW.bank_name, 'Undefined'),
            COALESCE(NEW.transaction_currency_code, 'N/A'),
            1,
            COALESCE(NEW.transaction_amount_bd, 0.00)
        )
        -- EL CONFLICT AHORA HACE MATCH CON LA NUEVA PRIMARY KEY
        ON CONFLICT (summary_date, host_type, issuer_bank, transaction_currency_code)
        DO UPDATE SET
            total_transactions = public.daily_transaction_summary.total_transactions + 1,
            total_amount = public.daily_transaction_summary.total_amount + EXCLUDED.total_amount;

    END IF;

    RETURN NEW;
END;
$function$;

CREATE OR REPLACE FUNCTION public.generate_random_system_stats(p_start_date timestamp without time zone DEFAULT '2024-01-01 00:00:00'::timestamp without time zone, p_end_date timestamp without time zone DEFAULT '2025-02-08 23:59:59'::timestamp without time zone)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$ 
DECLARE 
    v_ram_total BIGINT;
    v_ram_used BIGINT;
    v_ram_usage_percentage NUMERIC(5,2);
    v_storage_total BIGINT;
    v_storage_used BIGINT;
    v_storage_usage_percentage NUMERIC(5,2);
    v_network_usage_percentage NUMERIC(5,2);
    v_cpu_usage_percentage NUMERIC(5,2);
    v_connection_count INT;
    v_timestamp TIMESTAMP;
BEGIN 
    v_timestamp := p_start_date;
 
    WHILE v_timestamp <= p_end_date LOOP 
        -- 🔹 Generar valores aleatorios
        v_ram_total := (FLOOR(RANDOM() * 33) + 4) * 1024 * 1024 * 1024;  -- Entre 4GB y 32GB
        v_ram_used := FLOOR(RANDOM() * v_ram_total);
        v_ram_usage_percentage := ROUND((v_ram_used::NUMERIC / v_ram_total) * 100, 2);

        v_storage_total := (FLOOR(RANDOM() * 500) + 100) * 1024 * 1024 * 1024; -- Entre 100GB y 600GB
        v_storage_used := FLOOR(RANDOM() * v_storage_total);
        v_storage_usage_percentage := ROUND((v_storage_used::NUMERIC / v_storage_total) * 100, 2);

        -- ✅ Corregido: Convertir RANDOM() a NUMERIC antes de usar ROUND()
        v_network_usage_percentage := ROUND((RANDOM() * 100)::NUMERIC, 2); -- Entre 0% y 100%
        v_cpu_usage_percentage := ROUND((RANDOM() * 100)::NUMERIC, 2); -- Entre 0% y 100%

        v_connection_count := FLOOR(RANDOM() * 500) + 10; -- Entre 10 y 500 conexiones

        -- 🔹 Insertar datos en la tabla system_stats
        INSERT INTO system_stats (
            ram_total, ram_used, ram_usage_percentage, 
            storage_total, storage_used, storage_usage_percentage, 
            network_usage_percentage, cpu_usage_percentage, 
            connection_count, timestamp
        ) VALUES (
            v_ram_total, v_ram_used, v_ram_usage_percentage, 
            v_storage_total, v_storage_used, v_storage_usage_percentage, 
            v_network_usage_percentage, v_cpu_usage_percentage, 
            v_connection_count, v_timestamp
        );

        -- 🔄 Aumentar el timestamp en 15 minutos
        v_timestamp := v_timestamp + INTERVAL '15 minutes';
    END LOOP;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_client_activity_list(p_client_email character varying DEFAULT ''::character varying, p_client_user_name character varying DEFAULT ''::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_offset integer DEFAULT 0, p_limit integer DEFAULT 15)
 RETURNS SETOF client_activity_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
RETURN QUERY
SELECT cad.*
FROM client_activity_data cad
WHERE
    (p_client_email = '' OR cad.client_email = p_client_email) AND
    (p_client_user_name = '' OR cad.client_user_name = p_client_user_name) AND
    (p_terminal_serial_number = '' OR cad.terminal_serial_number = p_terminal_serial_number) AND
    (p_creation_date_start = '1993-11-01 00:00:00' OR cad.creation_date_time >= p_creation_date_start) AND
    (p_creation_date_end = '9999-12-31 23:59:59' OR cad.creation_date_time <= p_creation_date_end)
ORDER BY
    cad.creation_date_time DESC
OFFSET p_offset LIMIT p_limit;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_client_activity_pages(p_client_email character varying DEFAULT ''::character varying, p_client_user_name character varying DEFAULT ''::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_page_size integer DEFAULT 15)
 RETURNS integer
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
total_records INT;
BEGIN
SELECT COUNT(*)
INTO total_records
FROM client_activity_data cad
WHERE
    (p_client_email = '' OR cad.client_email = p_client_email) AND
    (p_client_user_name = '' OR cad.client_user_name = p_client_user_name) AND
    (p_terminal_serial_number = '' OR cad.terminal_serial_number = p_terminal_serial_number) AND
    (p_creation_date_start = '1993-11-01 00:00:00' OR cad.creation_date_time >= p_creation_date_start) AND
    (p_creation_date_end = '9999-12-31 23:59:59' OR cad.creation_date_time <= p_creation_date_end);

RETURN CEIL(total_records / p_page_size::NUMERIC);
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_comm_alarms(p_start_date timestamp without time zone, p_end_date timestamp without time zone, p_limit integer)
 RETURNS TABLE(id bigint, creation_date timestamp without time zone, server_name character varying, alarm_cause character varying, error_description text)
 LANGUAGE plpgsql
AS $function$
DECLARE
    v_final_limit integer;
BEGIN
    -- Si el parámetro es nulo o menor/igual a 0, por defecto asignamos 27
    v_final_limit := COALESCE(p_limit, 27);
    
    -- Validar de forma estricta que no supere el máximo de 27 solicitado
    IF v_final_limit > 27 OR v_final_limit <= 0 THEN
        v_final_limit := 27;
    END IF;

    RETURN QUERY 
    SELECT 
        c.id, 
        c.creation_date, 
        c.server_name, 
        c.alarm_cause, 
        c.error_description
    FROM public.comm_alarms_data c
    WHERE (p_start_date IS NULL OR c.creation_date >= p_start_date)
      AND (p_end_date IS NULL OR c.creation_date <= p_end_date)
    ORDER BY c.creation_date DESC
    LIMIT v_final_limit;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_currency_symbol(p_currency_number integer)
 RETURNS TABLE(p_currency_code text, p_numeric_code integer, p_symbol text)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY 
    SELECT currency_code, numeric_code, symbol
    FROM currency_symbols
    WHERE numeric_code = p_currency_number;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_daily_error_summary(p_start_date date, p_end_date date, p_host_type character varying, p_issuer_bank character varying)
 RETURNS SETOF daily_error_summary
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT *
    FROM public.daily_error_summary des
    WHERE (p_start_date IS NULL OR des.summary_date >= p_start_date)
      AND (p_end_date IS NULL OR des.summary_date <= p_end_date)
      AND (p_host_type IS NULL OR p_host_type = 'Undefined' OR p_host_type = '' OR des.host_type = p_host_type)
      AND (p_issuer_bank IS NULL OR p_issuer_bank = 'Undefined' OR p_issuer_bank = '' OR des.issuer_bank = p_issuer_bank)
    ORDER BY des.error_count DESC;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_daily_transaction_summary(p_start_date date, p_end_date date, p_host_type character varying, p_issuer_bank character varying)
 RETURNS SETOF daily_transaction_summary
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT *
    FROM public.daily_transaction_summary dts
    WHERE (p_start_date IS NULL OR dts.summary_date >= p_start_date)
      AND (p_end_date IS NULL OR dts.summary_date <= p_end_date)
      AND (p_host_type IS NULL OR p_host_type = 'Undefined' OR p_host_type = '' OR dts.host_type = p_host_type)
      AND (p_issuer_bank IS NULL OR p_issuer_bank = 'Undefined' OR p_issuer_bank = '' OR dts.issuer_bank = p_issuer_bank)
    ORDER BY dts.total_transactions DESC, dts.total_amount DESC;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_log_meta_list(p_error_code character varying DEFAULT 'Undefined'::character varying, p_server_name character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_module_id character varying DEFAULT 'Undefined'::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_offset integer DEFAULT 0, p_limit integer DEFAULT 15)
 RETURNS SETOF log_meta_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
RETURN QUERY
SELECT
    ed.*
FROM
    log_meta_data ed
WHERE

    (p_transaction_type = 'Undefined' OR ed.transaction_type = p_transaction_type) AND
    (
        (p_operation_id = 'Undefined' OR ed.operation_id = p_operation_id) OR
        (p_operation_id = 'Transaction' AND ed.operation_id IN ('Transaction', 'GatewayTransaction')) OR
        (p_operation_id = 'GatewayTransaction' AND ed.operation_id IN ('Transaction', 'GatewayTransaction'))
        )

   AND
    (p_error_code = 'Undefined' OR ed.error_code = p_error_code) AND
    (p_server_name = '' OR ed.server_name = p_server_name) AND
    (p_host_type = 'Undefined' OR ed.host_type = p_host_type) AND
    (p_terminal_serial_number = '' OR ed.terminal_serial_number = p_terminal_serial_number) AND
    (p_rrn = '' OR ed.rrn = p_rrn) AND
    (p_module_id = 'Undefined' OR ed.module_id = p_module_id) AND

    (p_creation_date_start = '1993-11-01 00:00:00' OR ed.creation_date_time >= p_creation_date_start) AND
    (p_creation_date_end = '9999-12-31 23:59:59' OR ed.creation_date_time <= p_creation_date_end)
ORDER BY
    ed.creation_timestamp DESC
OFFSET p_offset LIMIT p_limit;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_log_meta_pages(p_error_code character varying DEFAULT 'Undefined'::character varying, p_server_name character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_module_id character varying DEFAULT 'Undefined'::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_page_size integer DEFAULT 15)
 RETURNS integer
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
total_records INT;
BEGIN
SELECT COUNT(*)
INTO total_records
FROM log_meta_data ed
WHERE
    (p_transaction_type = 'Undefined' OR ed.transaction_type = p_transaction_type) AND
    (
        (p_operation_id = 'Undefined' OR ed.operation_id = p_operation_id) OR
        (p_operation_id = 'Transaction' AND ed.operation_id IN ('Transaction', 'GatewayTransaction')) OR
        (p_operation_id = 'GatewayTransaction' AND ed.operation_id IN ('Transaction', 'GatewayTransaction'))
        )

  AND
    (p_error_code = 'Undefined' OR ed.error_code = p_error_code) AND
    (p_server_name = '' OR ed.server_name = p_server_name) AND
    (p_host_type = 'Undefined' OR ed.host_type = p_host_type) AND
    (p_terminal_serial_number = '' OR ed.terminal_serial_number = p_terminal_serial_number) AND
    (p_rrn = '' OR ed.rrn = p_rrn) AND
    (p_module_id = 'Undefined' OR ed.module_id = p_module_id) AND
    (p_creation_date_start = '1993-11-01 00:00:00' OR ed.creation_date_time >= p_creation_date_start) AND
    (p_creation_date_end = '9999-12-31 23:59:59' OR ed.creation_date_time <= p_creation_date_end);

RETURN CEIL(total_records / p_page_size::NUMERIC);
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_original_transaction_data(p_transaction_id bigint DEFAULT '-1'::integer, p_host_type character varying DEFAULT 'Undefined'::character varying, p_transaction_message_type character varying DEFAULT 'Undefined'::character varying, p_transaction_status character varying DEFAULT 'Undefined'::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_authorization_id_response character varying DEFAULT ''::character varying)
 RETURNS SETOF transaction_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT * 
    FROM transaction_data
    WHERE
        -- Solo exigir transaction_type != 'Reverse' cuando el host sea CrediBancoDev o CrediBancoProd
          transaction_type != 'Reverse'
        AND (p_transaction_id = -1 OR transaction_id = p_transaction_id)
        AND (NULLIF(p_host_type, 'Undefined') IS NULL OR host_type = p_host_type)
        AND (NULLIF(p_transaction_message_type, 'Undefined') IS NULL OR transaction_message_type = p_transaction_message_type)
        AND (NULLIF(p_transaction_status, 'Undefined') IS NULL OR transaction_status = p_transaction_status)
        AND (NULLIF(p_terminal_serial_number, '') IS NULL OR terminal_serial_number = p_terminal_serial_number)
        AND (NULLIF(p_rrn, '') IS NULL OR rrn = p_rrn)
        AND (NULLIF(p_authorization_id_response, '') IS NULL OR authorization_id_response = p_authorization_id_response)
    ORDER BY
        creation_date_time DESC
    LIMIT 1;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_reverse(p_rrn character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying)
 RETURNS SETOF reverses_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
RETURN QUERY
SELECT * 
FROM reverses_data
WHERE 
    rrn = p_rrn
    AND reverse_status != 'WaitingResponse'
ORDER BY
    last_modification_date_time DESC;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_reverses_list(p_rrn character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying)
 RETURNS SETOF reverses_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
RETURN QUERY
SELECT *
FROM reverses_data
WHERE 
    (p_rrn = '' OR rrn = p_rrn) AND
    -- Cambiar lógica para "diferente de"
    (p_host_type = 'Undefined' OR host_type = p_host_type)
    AND must_be_reversed = true
    AND reverse_status != 'WaitingResponse'
ORDER BY
    last_modification_date_time DESC;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_reverses_meta_report_list(p_rrn character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_reverse_status character varying DEFAULT 'Undefined'::character varying, p_must_be_reversed boolean DEFAULT true, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_offset integer DEFAULT 0, p_limit integer DEFAULT 15)
 RETURNS SETOF reverses_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT *
    FROM reverses_data
    WHERE 
        (p_rrn = '' OR rrn = p_rrn) AND
        (p_host_type = 'Undefined' OR host_type = p_host_type) AND
        (p_reverse_status = 'Undefined' OR reverse_status = p_reverse_status) AND
        (p_must_be_reversed IS NULL OR must_be_reversed = p_must_be_reversed) AND
        (p_creation_date_start = '1993-11-01 00:00:00' OR last_modification_date_time >= p_creation_date_start) AND
        (p_creation_date_end = '9999-12-31 23:59:59' OR last_modification_date_time <= p_creation_date_end)
    ORDER BY last_modification_date_time DESC
    OFFSET p_offset LIMIT p_limit;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_system_stats_min_max(p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone)
 RETURNS TABLE(metric_name text, value numeric, type text, event_date timestamp without time zone, total_transactions bigint)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$ 
BEGIN 
    RETURN QUERY
    -- 🔹 Unión de todos los valores mínimos y máximos con su etiqueta correspondiente
    SELECT 
        'ram_total', 
        COALESCE(MIN(ram_total)::NUMERIC, 0), 
        'MIN', 
        MIN(timestamp),
        (SELECT COALESCE(SUM(tmd.total_transaction_approved_count + tmd.total_transaction_rejected_count), 0)::BIGINT
         FROM transactions_meta_data tmd
         WHERE tmd.report_time_period = 'Daily'
         AND DATE(tmd.date_time_initial) = DATE(MIN(system_stats.timestamp)))
    FROM system_stats
    WHERE system_stats.timestamp BETWEEN p_creation_date_start AND p_creation_date_end

    UNION ALL

    SELECT 
        'ram_total', 
        COALESCE(MAX(ram_total)::NUMERIC, 0), 
        'MAX', 
        MAX(timestamp),
        (SELECT COALESCE(SUM(tmd.total_transaction_approved_count + tmd.total_transaction_rejected_count), 0)::BIGINT
         FROM transactions_meta_data tmd
         WHERE tmd.report_time_period = 'Daily'
         AND DATE(tmd.date_time_initial) = DATE(MAX(system_stats.timestamp)))
    FROM system_stats
    WHERE system_stats.timestamp BETWEEN p_creation_date_start AND p_creation_date_end

    UNION ALL

    SELECT 
        'ram_used', 
        COALESCE(MIN(ram_used)::NUMERIC, 0), 
        'MIN', 
        MIN(timestamp),
        (SELECT COALESCE(SUM(tmd.total_transaction_approved_count + tmd.total_transaction_rejected_count), 0)::BIGINT
         FROM transactions_meta_data tmd
         WHERE tmd.report_time_period = 'Daily'
         AND DATE(tmd.date_time_initial) = DATE(MIN(system_stats.timestamp)))
    FROM system_stats
    WHERE system_stats.timestamp BETWEEN p_creation_date_start AND p_creation_date_end

    UNION ALL

    SELECT 
        'ram_used', 
        COALESCE(MAX(ram_used)::NUMERIC, 0), 
        'MAX', 
        MAX(timestamp),
        (SELECT COALESCE(SUM(tmd.total_transaction_approved_count + tmd.total_transaction_rejected_count), 0)::BIGINT
         FROM transactions_meta_data tmd
         WHERE tmd.report_time_period = 'Daily'
         AND DATE(tmd.date_time_initial) = DATE(MAX(system_stats.timestamp)))
    FROM system_stats
    WHERE system_stats.timestamp BETWEEN p_creation_date_start AND p_creation_date_end

    UNION ALL

    SELECT 
        'cpu_usage_percentage', 
        COALESCE(MIN(cpu_usage_percentage)::NUMERIC, 0), 
        'MIN', 
        MIN(timestamp),
        (SELECT COALESCE(SUM(tmd.total_transaction_approved_count + tmd.total_transaction_rejected_count), 0)::BIGINT
         FROM transactions_meta_data tmd
         WHERE tmd.report_time_period = 'Daily'
         AND DATE(tmd.date_time_initial) = DATE(MIN(system_stats.timestamp)))
    FROM system_stats
    WHERE system_stats.timestamp BETWEEN p_creation_date_start AND p_creation_date_end

    UNION ALL

    SELECT 
        'cpu_usage_percentage', 
        COALESCE(MAX(cpu_usage_percentage)::NUMERIC, 0), 
        'MAX', 
        MAX(timestamp),
        (SELECT COALESCE(SUM(tmd.total_transaction_approved_count + tmd.total_transaction_rejected_count), 0)::BIGINT
         FROM transactions_meta_data tmd
         WHERE tmd.report_time_period = 'Daily'
         AND DATE(tmd.date_time_initial) = DATE(MAX(system_stats.timestamp)))
    FROM system_stats
    WHERE system_stats.timestamp BETWEEN p_creation_date_start AND p_creation_date_end;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_system_stats_paginated(p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_offset integer DEFAULT 0, p_limit integer DEFAULT 15)
 RETURNS TABLE(p_system_stats_id integer, p_ram_total bigint, p_ram_used bigint, p_ram_usage_percentage numeric, p_storage_total bigint, p_storage_used bigint, p_storage_usage_percentage numeric, p_network_usage_percentage numeric, p_cpu_usage_percentage numeric, p_connection_count integer, p_timestamp timestamp without time zone)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY 
    SELECT 
        system_stats_id,
        ram_total,
        ram_used,
        ram_usage_percentage,
        storage_total,
        storage_used,
        storage_usage_percentage,
        network_usage_percentage,
        cpu_usage_percentage,
        connection_count,
        timestamp
    FROM system_stats
    WHERE 
        (p_creation_date_start = '1993-11-01 00:00:00' OR timestamp >= p_creation_date_start) 
        AND (p_creation_date_end = '9999-12-31 23:59:59' OR timestamp <= p_creation_date_end)
    ORDER BY timestamp DESC
    OFFSET p_offset LIMIT p_limit;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_system_stats_paginated(p_limit integer, p_offset integer)
 RETURNS TABLE(p_system_stats_id integer, p_ram_total bigint, p_ram_used bigint, p_ram_usage_percentage numeric, p_storage_total bigint, p_storage_used bigint, p_storage_usage_percentage numeric, p_network_usage_percentage numeric, p_cpu_usage_percentage numeric, p_connection_count integer, p_timestamp timestamp without time zone)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY 
    SELECT * FROM system_stats 
    ORDER BY timestamp DESC 
    LIMIT p_limit OFFSET p_offset;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_system_stats_total_pages(p_page_size integer)
 RETURNS integer
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    total_records INT;
    total_pages INT;
BEGIN
    -- 📊 Obtener el total de registros en la tabla
    SELECT COUNT(*) INTO total_records FROM system_stats;

    -- 📄 Calcular el número total de páginas
    total_pages := CEIL(total_records::DECIMAL / p_page_size);

    RETURN total_pages;
END;

$function$;

CREATE OR REPLACE FUNCTION public.get_today_transaction_reports(p_report_type text)
 RETURNS TABLE(transactions_meta_data_id bigint, date_time_initial timestamp with time zone, date_time_end timestamp with time zone, report_time_period text, host_type character varying, server_name text, time_zone character varying, terminal_serial_number character varying, merchant_type character varying, merchant_type_code character varying, transaction_type character varying, report_type text, total_transaction_approved_amount numeric, total_transaction_approved_count bigint, total_transaction_rejected_amount numeric, total_transaction_rejected_count bigint, creation_date_time timestamp with time zone, last_modification_date_time timestamp with time zone, transaction_currency_code text, transaction_currency_symbol text, transaction_currency_code_number integer)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
        t.transactions_meta_data_id,
        t.date_time_initial,
        t.date_time_end,
        t.report_time_period,
        t.host_type,
        t.server_name,
        t.time_zone,
        t.terminal_serial_number,
        t.merchant_type,
        t.merchant_type_code,
        t.transaction_type,
        t.report_type,
        t.total_transaction_approved_amount,
        t.total_transaction_approved_count,
        t.total_transaction_rejected_amount,
        t.total_transaction_rejected_count,
        t.creation_date_time,
        t.last_modification_date_time,
        t.transaction_currency_code,
        t.transaction_currency_symbol,
        t.transaction_currency_code_number
    FROM transactions_meta_data t
    WHERE (p_report_type=''  OR t.report_type = p_report_type)
      AND (
          (t.report_time_period = 'Daily' AND t.date_time_initial >= date_trunc('day', NOW()) AND t.date_time_end <= date_trunc('day', NOW()) + interval '1 day' - interval '1 second')
          OR (t.report_time_period = 'Weekly' AND t.date_time_initial >= date_trunc('week', NOW()) AND t.date_time_end <= date_trunc('week', NOW()) + interval '1 week' - interval '1 second')
          OR (t.report_time_period = 'Monthly' AND t.date_time_initial >= date_trunc('month', NOW()) AND t.date_time_end <= date_trunc('month', NOW()) + interval '1 month' - interval '1 second')
          OR (t.report_time_period = 'Annual' AND t.date_time_initial >= date_trunc('year', NOW()) AND t.date_time_end <= date_trunc('year', NOW()) + interval '1 year' - interval '1 second')
      ) 
    ORDER BY t.creation_date_time DESC;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_today_transaction_reports(p_report_type text DEFAULT 'Universal'::text, p_host_type character varying DEFAULT 'Undefined'::character varying)
 RETURNS TABLE(transactions_meta_data_id bigint, date_time_initial timestamp with time zone, date_time_end timestamp with time zone, report_time_period text, host_type character varying, server_name text, time_zone character varying, terminal_serial_number character varying, merchant_type character varying, merchant_type_code character varying, transaction_type character varying, report_type text, total_transaction_approved_amount numeric, total_transaction_approved_count bigint, total_transaction_rejected_amount numeric, total_transaction_rejected_count bigint, creation_date_time timestamp with time zone, last_modification_date_time timestamp with time zone, transaction_currency_code text, transaction_currency_symbol text, transaction_currency_code_number integer, total_txns_commerr_count bigint, last_update_pm_date_time timestamp with time zone, total_txns_rejected_pm bigint, total_txns_approved_pm bigint, total_transaction_commerr_pm bigint, ram_total_pm numeric, ram_used_pm numeric, ram_usage_percentage_pm numeric, storage_total_pm numeric, storage_used_pm numeric, storage_usage_percentage_pm numeric, network_usage_percentage_pm numeric, cpu_usage_percentage_pm numeric, connection_count_pm integer)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
   DECLARE  p_transaction_date_time TIMESTAMP WITH TIME ZONE=NOW();

BEGIN
   RETURN QUERY
        SELECT 
            t.transactions_meta_data_id,
            t.date_time_initial,
            t.date_time_end,
            COALESCE(t.report_time_period, '') AS report_time_period,
            COALESCE(t.host_type, '') AS host_type,
            COALESCE(t.server_name, '') AS server_name,
            COALESCE(t.time_zone, '') AS time_zone,
            COALESCE(t.terminal_serial_number, '') AS terminal_serial_number,
            COALESCE(t.merchant_type, '') AS merchant_type,
            COALESCE(t.merchant_type_code, '') AS merchant_type_code,
            COALESCE(t.transaction_type, '') AS transaction_type,
            COALESCE(t.report_type, '') AS report_type,
            t.total_transaction_approved_amount,
            t.total_transaction_approved_count,
            t.total_transaction_rejected_amount,
            t.total_transaction_rejected_count,
            t.creation_date_time,
            t.last_modification_date_time,
            COALESCE(t.transaction_currency_code, '') AS transaction_currency_code,
            COALESCE(t.transaction_currency_symbol, '') AS transaction_currency_symbol,
            t.transaction_currency_code_number,
            t.total_txns_commerr_count,
			t.last_update_pm_date_time, 
			t.total_txns_rejected_pm,
			t.total_txns_approved_pm,
			t.total_transaction_commerr_pm,
			t.ram_total_pm::numeric,
			t.ram_used_pm::numeric,
			t.ram_usage_percentage_pm,
			t.storage_total_pm::numeric,
			t.storage_used_pm::numeric,
			t.storage_usage_percentage_pm,
			t.network_usage_percentage_pm,
			t.cpu_usage_percentage_pm,
			t.connection_count_pm
        FROM transactions_meta_data t
        WHERE (p_report_type = '' OR t.report_type = p_report_type)
          AND   (p_host_type = 'Undefined' OR t.host_type = p_host_type)

        AND (
            (t.report_time_period = 'Daily' AND t.date_time_initial >= date_trunc('day',p_transaction_date_time) AND t.date_time_end <= date_trunc('day', NOW()) + interval '1 day' - interval '1 second')
            OR (t.report_time_period = 'Weekly' AND t.date_time_initial >= date_trunc('week', p_transaction_date_time) AND t.date_time_end <= date_trunc('week', NOW()) + interval '1 week' - interval '1 second')
            OR (t.report_time_period = 'Monthly' AND t.date_time_initial >= date_trunc('month', p_transaction_date_time) AND t.date_time_end <= date_trunc('month', NOW()) + interval '1 month' - interval '1 second')
        )
        ORDER BY t.creation_date_time DESC;

END;
$function$;

CREATE OR REPLACE FUNCTION public.get_total_pages_reverses_meta(p_rrn character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_reverse_status character varying DEFAULT 'Undefined'::character varying, p_must_be_reversed boolean DEFAULT true, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_limit integer DEFAULT 15)
 RETURNS integer
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    total_count INT;
BEGIN
    -- Contar el número total de registros que cumplen las condiciones
    SELECT COUNT(*)
    INTO total_count
    FROM reverses_data
    WHERE 
        (p_rrn = '' OR rrn = p_rrn) AND
        (p_host_type = 'Undefined' OR host_type = p_host_type) AND
        (p_reverse_status = 'Undefined' OR reverse_status = p_reverse_status) AND
        (p_must_be_reversed IS NULL OR must_be_reversed = p_must_be_reversed) AND
        (p_creation_date_start = '1993-11-01 00:00:00' OR last_modification_date_time >= p_creation_date_start) AND
        (p_creation_date_end = '9999-12-31 23:59:59' OR last_modification_date_time <= p_creation_date_end);

    -- Calcular el número de páginas basado en el tamaño de página
    RETURN CEIL(total_count::NUMERIC / p_limit::NUMERIC);
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_total_transaction_pages(p_server_name character varying DEFAULT ''::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_page_size integer DEFAULT 10)
 RETURNS integer
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
total_records INT;
BEGIN
SELECT COUNT(*)
INTO total_records
FROM transaction_data
WHERE
    (p_operation_id = 'Undefined' OR  operation_id = p_operation_id) AND
    (p_transaction_type = 'Undefined' OR  transaction_type = p_transaction_type) AND
    (p_server_name = '' OR server_name = p_server_name) AND
    (p_host_type = 'Undefined' OR host_type = p_host_type) AND
    (p_terminal_serial_number = '' OR terminal_serial_number = p_terminal_serial_number) AND
    (p_rrn = '' OR rrn = p_rrn) AND
    (p_creation_date_start = '1993-11-01 00:00:00' OR creation_date_time >= p_creation_date_start) AND
    (p_creation_date_end = '9999-12-31 23:59:59' OR creation_date_time <= p_creation_date_end);

RETURN CEIL(total_records / p_page_size::NUMERIC);
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_transaction_by_type_and_rrn(p_rrn character varying, p_transaction_type character varying)
 RETURNS SETOF transaction_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT *
    FROM transaction_data
    WHERE
        rrn = p_rrn
        AND transaction_type = p_transaction_type
        AND transaction_type != 'Reverse'
    ORDER BY
        creation_date_time DESC
    LIMIT 1;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_transaction_data(p_transaction_id bigint DEFAULT '-1'::integer, p_host_type character varying DEFAULT 'Undefined'::character varying, p_transaction_message_type character varying DEFAULT 'Undefined'::character varying, p_transaction_status character varying DEFAULT 'Undefined'::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_authorization_id_response character varying DEFAULT ''::character varying)
 RETURNS SETOF transaction_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$ 
BEGIN
    RETURN QUERY
    SELECT *
    FROM transaction_data
    WHERE
        (p_transaction_id = -1 OR transaction_id = p_transaction_id) AND
        (p_host_type = 'Undefined' OR host_type = p_host_type) AND
        (p_transaction_message_type = 'Undefined' OR transaction_message_type = p_transaction_message_type) AND
        (p_transaction_status = 'Undefined' OR transaction_status = p_transaction_status) AND
        (p_terminal_serial_number = '' OR terminal_serial_number = p_terminal_serial_number) AND
        (p_rrn = '' OR rrn = p_rrn) AND
        (p_authorization_id_response = '' OR authorization_id_response = p_authorization_id_response)
    ORDER BY
        creation_date_time DESC
    LIMIT 1;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_transaction_reports(p_report_type text, p_page integer, p_page_size integer)
 RETURNS TABLE(transactions_meta_data_id bigint, date_time_initial timestamp with time zone, date_time_end timestamp with time zone, report_time_period text, host_type character varying, server_name text, time_zone character varying, terminal_serial_number character varying, merchant_type character varying, merchant_type_code character varying, transaction_type character varying, report_type text, total_transaction_approved_amount numeric, total_transaction_approved_count bigint, total_transaction_rejected_amount numeric, total_transaction_rejected_count bigint, creation_date_time timestamp with time zone, last_modification_date_time timestamp with time zone, transaction_currency_code text, transaction_currency_symbol text, transaction_currency_code_number integer)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
        t.transactions_meta_data_id,
        t.date_time_initial,
        t.date_time_end,
        t.report_time_period,
        t.host_type,
        t.server_name,
        t.time_zone, 
        t.terminal_serial_number,
        t.merchant_type,
        t.merchant_type_code,
        t.transaction_type,
        t.report_type,
        t.total_transaction_approved_amount,
        t.total_transaction_approved_count,
        t.total_transaction_rejected_amount,
        t.total_transaction_rejected_count,
        t.creation_date_time,
        t.last_modification_date_time,
        t.transaction_currency_code,
        t.transaction_currency_symbol,
        t.transaction_currency_code_number
    FROM transactions_meta_data t
    WHERE (p_report_type='' OR t.report_type = p_report_type)
    ORDER BY t.creation_date_time DESC
    LIMIT p_page_size OFFSET (p_page - 1) * p_page_size;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_transactions_list(p_server_name character varying DEFAULT ''::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_creation_date_start timestamp without time zone DEFAULT '1993-11-01 00:00:00'::timestamp without time zone, p_creation_date_end timestamp without time zone DEFAULT '9999-12-31 23:59:59'::timestamp without time zone, p_offset integer DEFAULT 0, p_limit integer DEFAULT 15)
 RETURNS SETOF transaction_data
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
RETURN QUERY
SELECT
    *
FROM
    transaction_data
WHERE
    (p_operation_id = 'Undefined' OR  operation_id = p_operation_id) AND
    (p_transaction_type = 'Undefined' OR  transaction_type = p_transaction_type) AND
    (p_server_name = '' OR server_name = p_server_name) AND
    (p_host_type = 'Undefined' OR host_type = p_host_type) AND
    (p_terminal_serial_number = '' OR terminal_serial_number = p_terminal_serial_number) AND
    (p_rrn = '' OR rrn = p_rrn) AND
    (p_creation_date_start = '1993-11-01 00:00:00' OR creation_date_time >= p_creation_date_start) AND
    (p_creation_date_end = '9999-12-31 23:59:59' OR creation_date_time <= p_creation_date_end)
ORDER BY
    creation_timestamp DESC
OFFSET p_offset LIMIT p_limit;
END;
$function$;

CREATE OR REPLACE FUNCTION public.get_txns_tobe_reversed(p_host_type character varying DEFAULT 'Undefined'::character varying)
 RETURNS SETOF bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
RETURN QUERY
SELECT 
    transaction_id
FROM 
    transaction_data
WHERE
    (p_host_type = 'Undefined' OR host_type = p_host_type) AND
    (transaction_status = 'MustBeReversed') 
ORDER BY
    creation_timestamp DESC;
END; 
$function$;

CREATE OR REPLACE FUNCTION public.insert_comm_alarm(p_server_name character varying, p_alarm_cause character varying, p_error_description text)
 RETURNS bigint
 LANGUAGE plpgsql
AS $function$
DECLARE
    v_inserted_id bigint;
BEGIN
    INSERT INTO public.comm_alarms_data (
        server_name,
        alarm_cause,
        error_description,
        creation_date
    ) VALUES (
        p_server_name,
        p_alarm_cause,
        p_error_description,
        CURRENT_TIMESTAMP
    ) RETURNING id INTO v_inserted_id;

    RETURN v_inserted_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.insert_reeverse_data(p_transaction_id bigint DEFAULT '-1'::integer, p_authorization_id_response character varying DEFAULT ''::character varying, p_card_type character varying DEFAULT ''::character varying, p_emv_data text DEFAULT ''::text, p_host_type character varying DEFAULT 'Undefined'::character varying, p_isoascii text DEFAULT ''::text, p_merchant_type character varying DEFAULT 'Undefined'::character varying, p_original_data_elements text DEFAULT ''::text, p_payment_plan_type character varying DEFAULT 'Undefined'::character varying, p_point_of_service_entry_mode character varying DEFAULT 'Undefined'::character varying, p_response_code character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_transaction_amount character varying DEFAULT ''::character varying, p_transaction_datajson text DEFAULT ''::text, p_transaction_direction character varying DEFAULT 'Undefined'::character varying, p_transaction_message_type character varying DEFAULT 'Undefined'::character varying, p_transaction_status character varying DEFAULT 'Undefined'::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_must_be_reversed boolean DEFAULT false)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    inserted_id bigint;
BEGIN
    -- Verificar si el transaction_id ya existe
    IF (SELECT COUNT(*) FROM reverses_data WHERE transaction_id = p_transaction_id) > 0 THEN
        RETURN -1; -- No insertar y retornar -1 
    ELSE
        INSERT INTO reverses_data (
            transaction_id,
            authorization_id_response,
            card_type,
            emv_data,
            host_type,
            isoascii, 
            last_modification_date_time,
            merchant_type,
            original_data_elements,
            payment_plan_type,
            point_of_service_entry_mode,
            response_code,
            rrn,
            terminal_serial_number,
            transaction_amount,
            transaction_datajson,
            transaction_direction,
            transaction_message_type,
            transaction_status,
            transaction_type,
            reverse_status ,
            must_be_reversed 

        ) VALUES (
            p_transaction_id,
            p_authorization_id_response,
            p_card_type,
            p_emv_data,
            p_host_type,
            p_isoascii,
            CURRENT_TIMESTAMP,
            p_merchant_type,
            p_original_data_elements,
            p_payment_plan_type,
            p_point_of_service_entry_mode,
            p_response_code,
            p_rrn,
            p_terminal_serial_number,
            p_transaction_amount,
            p_transaction_datajson,
            p_transaction_direction,
            p_transaction_message_type,
            p_transaction_status,
            p_transaction_type,
            'Created' ,
            p_must_be_reversed 
        ) RETURNING transaction_id INTO inserted_id;

        RETURN inserted_id;
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.insert_system_stats(p_ram_total bigint, p_ram_used bigint, p_ram_usage_percentage numeric, p_storage_total bigint, p_storage_used bigint, p_storage_usage_percentage numeric, p_network_usage_percentage numeric, p_cpu_usage_percentage numeric)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$

DECLARE
    last_timestamp TIMESTAMP;
    transactions_number INT; 
    connection_count INT;
    time_range_start TIMESTAMP;
    v_system_id BIGINT;  -- Variable para almacenar el ID insertado

BEGIN
    -- Obtener el último timestamp insertado en system_stats
    SELECT MAX(timestamp) INTO last_timestamp FROM system_stats;
    
    -- Definir el inicio del rango de tiempo
    IF last_timestamp IS NOT NULL THEN
        time_range_start := last_timestamp;
    ELSE
        time_range_start := NOW() - INTERVAL '20 minutes';
    END IF;

    -- Contar las transacciones en el rango de tiempo
    SELECT COUNT(*) INTO transactions_number
    FROM transaction_data
    WHERE creation_timestamp BETWEEN time_range_start AND NOW();

    -- Obtener el número de conexiones activas
    SELECT COUNT(*) INTO connection_count
    FROM pg_stat_activity
    WHERE state = 'active'; 

    -- Insertar el nuevo registro y obtener el ID generado
    INSERT INTO system_stats (
        ram_total, ram_used, ram_usage_percentage, 
        storage_total, storage_used, storage_usage_percentage, 
        network_usage_percentage, cpu_usage_percentage, 
        connection_count, transactions_number
    ) VALUES (
        p_ram_total, p_ram_used, p_ram_usage_percentage, 
        p_storage_total, p_storage_used, p_storage_usage_percentage, 
        p_network_usage_percentage, p_cpu_usage_percentage, 
        connection_count, transactions_number
    )
    RETURNING system_stats_id INTO v_system_id;

    -- Retornar el ID insertado
    RETURN v_system_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.insert_transaction_data(p_is_initial boolean DEFAULT false, p_time_zone character varying DEFAULT 'UTC'::character varying, p_creation_date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP, p_last_modification_date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP, p_error_code character varying DEFAULT 'Undefined'::character varying, p_error_code_number integer DEFAULT 0, p_error_message character varying DEFAULT ''::character varying, p_card_bin character varying DEFAULT ''::character varying, p_card_number character varying DEFAULT ''::character varying, p_affiliation character varying DEFAULT ''::character varying, p_bank_name character varying DEFAULT ''::character varying, p_merchant_name character varying DEFAULT ''::character varying, p_merchant_type_code character varying DEFAULT ''::character varying, p_merchant_address character varying DEFAULT ''::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_device_type character varying DEFAULT 'Undefined'::character varying, p_client_id character varying DEFAULT 'VIERGE'::character varying, p_fiid character varying DEFAULT ''::character varying, p_transaction_id_mpp character varying DEFAULT ''::character varying, p_authorization_id_response character varying DEFAULT ''::character varying, p_bank character varying DEFAULT 'Undefined'::character varying, p_card_type character varying DEFAULT 'Undefined'::character varying, p_client_email character varying DEFAULT ''::character varying, p_clientip character varying DEFAULT ''::character varying, p_client_location character varying DEFAULT ''::character varying, p_client_user_name character varying DEFAULT ''::character varying, p_db_kcv text DEFAULT ''::text, p_device_model character varying DEFAULT ''::character varying, p_emv_data bytea DEFAULT '\x5c78'::bytea, p_host_type character varying DEFAULT 'Undefined'::character varying, p_isoasciirequest bytea DEFAULT '\x5c78'::bytea, p_isoasciiresponse bytea DEFAULT '\x5c78'::bytea, p_key_serial_number text DEFAULT ''::text, p_merchant_type character varying DEFAULT 'Undefined'::character varying, p_meta_data bytea DEFAULT '\x5c78'::bytea, p_original_transaction_id bigint DEFAULT NULL::bigint, p_payment_plan_type character varying DEFAULT 'Undefined'::character varying, p_point_of_service_entry_mode character varying DEFAULT 'Undefined'::character varying, p_response_code character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_system_trace_audit_number character varying DEFAULT ''::character varying, p_server_name text DEFAULT ''::text, p_terminal_serial_number character varying DEFAULT ''::character varying, p_transaction_amount character varying DEFAULT ''::character varying, p_transaction_direction character varying DEFAULT 'Request'::character varying, p_transaction_message_type character varying DEFAULT 'Undefined'::character varying, p_transaction_status character varying DEFAULT 'ReceivedInWebService'::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_transaction_currency_code_number integer DEFAULT 484, p_ram_total numeric DEFAULT 0, p_ram_used numeric DEFAULT 0, p_ram_usage_percentage numeric DEFAULT 0, p_storage_total numeric DEFAULT 0, p_storage_used numeric DEFAULT 0, p_storage_usage_percentage numeric DEFAULT 0, p_network_usage_percentage numeric DEFAULT 0, p_cpu_usage_percentage numeric DEFAULT 0, p_uuid bytea DEFAULT '\x5c78'::bytea)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    inserted_id BIGINT; 
    p_transaction_amount_bd NUMERIC(10,2);  
    p_is_approved BOOLEAN;
    p_transaction_currency_symbol TEXT;
    p_transaction_currency_code TEXT;
        p_connection_count_pm INT := 0;
        p_can_insert BOOLEAN := TRUE;

BEGIN
    -- 1️⃣ Intentar convertir transaction_amount a NUMERIC
    BEGIN
        p_transaction_amount_bd := (NULLIF(p_transaction_amount, '')::NUMERIC / 100)::NUMERIC(10,2);
    EXCEPTION 
        WHEN others THEN 
            p_transaction_amount_bd := 0;
    END;
   
   /* SELECT COUNT(*) INTO p_connection_count_pm
    FROM pg_stat_activity
    WHERE state = 'active'; 

    -- 2️⃣ Determinar si la transacción fue aprobada
    p_is_approved := (p_transaction_status = 'Approved');
*/
    -- 3️⃣ Obtener el símbolo y código de la moneda
 SELECT 
    COALESCE(currency_code, '') AS currency_code,
    COALESCE(symbol, '') AS symbol
        INTO 
            p_transaction_currency_code, 
            p_transaction_currency_symbol
        FROM 
            currency_symbols
        WHERE 
            numeric_code = p_transaction_currency_code_number;

    -- 4️⃣ Llamar a la función para guardar los reportes
    
    
    /*
*/


    -- 5️⃣ Verificar si el UUID ya existe y si hay cambios para actualizar
    IF p_is_initial=FALSE THEN

       


                    -- Verificamos si los valores cambiaron antes de actualizar
            UPDATE transaction_data SET
                time_zone = p_time_zone,
                last_modification_date_time = CURRENT_TIMESTAMP,
                error_code = p_error_code,
                error_code_number = p_error_code_number,
                error_message = p_error_message,
                operation_id = p_operation_id,
                authorization_id_response = p_authorization_id_response,
                bank = p_bank,
                card_type = p_card_type,
                client_email = p_client_email,
                clientip = p_clientip,
                client_location = p_client_location,
                client_user_name = p_client_user_name,
                device_type = p_device_type,
                client_id = p_client_id,
                fiid = p_fiid,


                db_kcv = p_db_kcv,
                device_model = p_device_model,
                emv_data = p_emv_data,
                host_type = p_host_type,
                isoasciirequest = p_isoasciirequest,
                isoasciiresponse = p_isoasciiresponse,
                key_serial_number = p_key_serial_number,
                merchant_type = p_merchant_type,
                meta_data = p_meta_data,
                original_transaction_id = COALESCE(p_original_transaction_id, -1),
                payment_plan_type = p_payment_plan_type,
                point_of_service_entry_mode = p_point_of_service_entry_mode,
                response_code = p_response_code,
                rrn = p_rrn,
                server_name = p_server_name,
                terminal_serial_number = UPPER(p_terminal_serial_number),
                transaction_amount = p_transaction_amount,
                transaction_direction = p_transaction_direction,
                transaction_message_type = p_transaction_message_type,
                transaction_status = p_transaction_status,
                transaction_type = p_transaction_type,
                card_bin = p_card_bin,
                card_number = p_card_number,
                affiliation = p_affiliation,
                bank_name = p_bank_name,
                merchant_name = p_merchant_name,
                merchant_type_code = p_merchant_type_code, 
                merchant_address = p_merchant_address,
                transaction_amount_bd = p_transaction_amount_bd,
                transaction_currency_code = p_transaction_currency_code,
                transaction_currency_symbol = p_transaction_currency_symbol,
                transaction_currency_code_number = p_transaction_currency_code_number,
                ram_total_pm = p_ram_total,
                ram_used_pm = p_ram_used,
           
                 
                ram_usage_percentage_pm = p_ram_usage_percentage,
                storage_total_pm = p_storage_total,
                storage_used_pm = p_storage_used,
                storage_usage_percentage_pm = p_storage_usage_percentage,
                network_usage_percentage_pm = p_network_usage_percentage,
                cpu_usage_percentage_pm = p_cpu_usage_percentage,
                connection_count_pm = 0,
                operation_duration = EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - creation_date_time))

            WHERE ( system_trace_audit_number=p_system_trace_audit_number and rrn=p_rrn and terminal_serial_number=p_terminal_serial_number and transaction_amount=p_transaction_amount and transaction_type=p_transaction_type);





       /* SELECT COUNT(*) INTO p_connection_count_pm
            FROM pg_stat_activity
            WHERE state = 'active'; 

            -- 2️⃣ Determinar si la transacción fue aprobada
            p_is_approved := (p_transaction_status = 'Approved');
            
                PERFORM save_transaction_reports(
                    p_host_type,           
                    p_server_name,         
                    p_time_zone,           
                    p_terminal_serial_number, 
                    p_merchant_type,       
                    p_merchant_type_code,  
                    p_transaction_type,    
                    p_transaction_amount_bd, 
                    p_transaction_currency_code_number,
                    1,                     
                    p_is_approved         ,
                    p_transaction_status,
                    
                p_ram_total_pm ,
                p_ram_used_pm ,
                p_ram_usage_percentage_pm ,
                p_storage_total_pm ,
                p_storage_used_pm ,
                p_storage_usage_percentage_pm ,
                p_network_usage_percentage_pm ,
                p_cpu_usage_percentage_pm 

            
                );*/

            RETURN 0;
 
    ELSE 


      
        -- 6️⃣ Insertar la transacción en la base de datos
        INSERT INTO transaction_data (
            time_zone, creation_date_time, last_modification_date_time, 
            error_code, error_code_number, error_message, 
            operation_id, authorization_id_response, bank, 
            card_type, client_email, clientip, client_location, client_user_name, 
            db_kcv, device_model, emv_data, host_type, isoasciirequest, isoasciiresponse, 
            key_serial_number, merchant_type, meta_data, original_transaction_id, 
            payment_plan_type, point_of_service_entry_mode, response_code, 
            rrn, server_name, terminal_serial_number, transaction_amount, 
            transaction_direction, transaction_message_type, transaction_status, 
            transaction_type, uuid, card_bin, card_number, affiliation, 
            bank_name, merchant_name, merchant_type_code, merchant_address, 
            transaction_amount_bd, transaction_currency_code, transaction_currency_symbol, transaction_currency_code_number,
               ram_total_pm,

                 ram_used_pm,
                ram_usage_percentage_pm,
                storage_total_pm,
                storage_used_pm,
                storage_usage_percentage_pm, 
                network_usage_percentage_pm, 
                cpu_usage_percentage_pm,
                connection_count_pm,device_type,client_id,system_trace_audit_number,fiid, transaction_id_mpp
 
 
        ) VALUES ( 
            p_time_zone, p_creation_date_time, p_last_modification_date_time, 
            p_error_code, p_error_code_number, p_error_message, 
            p_operation_id, p_authorization_id_response, p_bank, 
            p_card_type, p_client_email, p_clientip, p_client_location, p_client_user_name, 
            p_db_kcv, p_device_model, p_emv_data, p_host_type, p_isoasciirequest, p_isoasciiresponse, 
            p_key_serial_number, p_merchant_type, p_meta_data, 
            COALESCE(p_original_transaction_id, -1), 
            p_payment_plan_type, p_point_of_service_entry_mode, p_response_code, 
            p_rrn, p_server_name, UPPER(p_terminal_serial_number), p_transaction_amount, 
            p_transaction_direction, p_transaction_message_type, p_transaction_status, 
            p_transaction_type, p_uuid, p_card_bin, p_card_number, p_affiliation, 
            p_bank_name, p_merchant_name, p_merchant_type_code, p_merchant_address, 
            p_transaction_amount_bd, p_transaction_currency_code, p_transaction_currency_symbol,p_transaction_currency_code_number,

               p_ram_total,
                p_ram_used,
                p_ram_usage_percentage,
                p_storage_total,
                p_storage_used, 
                p_storage_usage_percentage,
                p_network_usage_percentage,
                p_cpu_usage_percentage,
                0,p_device_type,p_client_id,p_system_trace_audit_number,p_fiid,p_transaction_id_mpp
        ) RETURNING transaction_id INTO inserted_id;
 
        RETURN inserted_id;
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.log_exists(p_uuid bytea)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
v_log_id BIGINT;
BEGIN
SELECT log_id
INTO v_log_id
FROM log_meta_data
WHERE uuid = p_uuid;

IF FOUND THEN
        RETURN v_log_id;
ELSE
        RETURN -1::BIGINT; -- Aseguramos que el valor de retorno es BIGINT
END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.log_meta_report_today(p_host_type text DEFAULT NULL::text, p_transaction_type text DEFAULT NULL::text, p_operation_id text DEFAULT NULL::text, p_terminal_serial_number text DEFAULT NULL::text)
 RETURNS TABLE(v_uuid bytea, v_log_id bigint, v_bank character varying, v_client_email character varying, v_client_geolocation character varying, v_clientip character varying, v_client_user_name character varying, v_creation_date_time timestamp without time zone, v_device_model character varying, v_error_code character varying, v_error_code_number integer, v_error_message character varying, v_host_type character varying, v_last_modification_date_time timestamp without time zone, v_merchant_type character varying, v_module_id character varying, v_operation_id character varying, v_point_of_service_entry_mode character varying, v_response_code character varying, v_rrn character varying, v_server_name character varying, v_terminal_serial_number character varying, v_transaction_amount character varying, v_transaction_type character varying, v_operation_duration double precision, v_time_zone character varying, v_creation_timestamp timestamp without time zone, v_card_type character varying, v_card_bin character varying, v_card_number character varying, v_affiliation character varying, v_merchant_name character varying, v_merchant_type_code character varying, v_merchant_address character varying, v_bank_name character varying)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
        uuid,
        log_id AS v_log_id, 
        bank AS v_bank, 
        client_email AS v_client_email, 
        client_geolocation AS v_client_geolocation, 
        clientip AS v_clientip, 
        client_user_name AS v_client_user_name, 
        creation_date_time AS v_creation_date_time, 
        device_model AS v_device_model, 
        error_code AS v_error_code, 
        error_code_number AS v_error_code_number, 
        error_message AS v_error_message, 
        host_type AS v_host_type, 
        last_modification_date_time AS v_last_modification_date_time, 
        merchant_type AS v_merchant_type, 
        module_id AS v_module_id, 
        operation_id AS v_operation_id, 
        point_of_service_entry_mode AS v_point_of_service_entry_mode, 
        response_code AS v_response_code, 
        rrn AS v_rrn, 
        server_name AS v_server_name, 
        terminal_serial_number AS v_terminal_serial_number, 
        transaction_amount AS v_transaction_amount, 
        transaction_type AS v_transaction_type, 
        operation_duration::DOUBLE PRECISION AS v_operation_duration,  -- Conversión a DOUBLE PRECISION
        time_zone AS v_time_zone, 
        creation_timestamp AS v_creation_timestamp, 
        card_type AS v_card_type, 
        card_bin AS v_card_bin, 
        card_number AS v_card_number, 
        affiliation AS v_affiliation, 
        merchant_name AS v_merchant_name, 
        merchant_type_code AS v_merchant_type_code, 
        merchant_address AS v_merchant_address, 
        bank_name AS v_bank_name
    FROM 
        public.log_meta_data
    WHERE 
        creation_date_time::date = CURRENT_DATE and response_code <>''  AND error_code<>'AuthError' and
         ((p_host_type IS NULL OR p_host_type<>'') OR host_type = p_host_type) AND
         ((p_transaction_type IS NULL OR p_transaction_type<>'') OR transaction_type = p_transaction_type)  
    ORDER BY 
        creation_date_time DESC;

END;
$function$;

CREATE OR REPLACE FUNCTION public.log_meta_report_today_percentage(p_host_type text DEFAULT NULL::text, p_transaction_type text DEFAULT NULL::text, p_operation_id text DEFAULT NULL::text, p_terminal_serial_number text DEFAULT NULL::text)
 RETURNS TABLE(total_transactions bigint, total_ok bigint, total_failed bigint, percentage_ok numeric, percentage_failed numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) AS total_transactions,
        COUNT(*) FILTER (WHERE response_code = '00' ) AS total_ok,
        COUNT(*) FILTER (WHERE response_code <> '00') AS total_failed,
        ROUND((COUNT(*) FILTER (WHERE response_code = '00')::NUMERIC / COUNT(*) * 100), 2) AS percentage_ok,
        ROUND((COUNT(*) FILTER (WHERE response_code <> '00')::NUMERIC / COUNT(*) * 100), 2) AS percentage_failed
    FROM 
        public.log_meta_data
    WHERE 
        creation_date_time::date = CURRENT_DATE AND   response_code <>'' and   error_code<>'AuthError' AND
        ((p_host_type IS NULL OR p_host_type = '') OR host_type = p_host_type) AND 
        ((p_transaction_type IS NULL OR p_transaction_type = '') OR transaction_type = p_transaction_type) AND
        ((p_operation_id IS NULL OR p_operation_id = '') OR operation_id = p_operation_id) AND
        ((p_terminal_serial_number IS NULL OR p_terminal_serial_number = '') OR terminal_serial_number = p_terminal_serial_number);

END;
$function$;

CREATE OR REPLACE FUNCTION public.migrate_all_txn_to_reports()
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$ 
DECLARE
    total_records INT;  -- Total de registros en transaction_data
    batch_size INT := 20;  -- Tamaño del lote a procesar
    num_batches INT;  -- Número total de lotes a procesar
    current_index INT := 0; -- Índice actual para iterar
BEGIN
    -- 1️⃣ Obtener el total de registros en transaction_data
    SELECT COUNT(*) INTO total_records FROM public.transaction_data;
    
    -- 2️⃣ Calcular el número total de lotes necesarios
    num_batches := CEIL(total_records::NUMERIC / batch_size);

    RAISE NOTICE 'Total registros: %, Número de lotes: %', total_records, num_batches;

    -- 3️⃣ Iterar en lotes de 20 registros
    FOR current_index IN 0..(num_batches - 1) LOOP
        BEGIN
            -- Calcular el índice inicial del lote actual
            RAISE NOTICE 'Procesando lote % de %, registros % a %',
                current_index + 1, num_batches, current_index * batch_size, (current_index * batch_size) + batch_size - 1;

            -- Llamar a migrate_existing_transactions_to_reports con el rango de datos
            PERFORM migrate_existing_transactions_to_reports(
                current_index * batch_size,  -- Índice inicial
                batch_size                   -- Tamaño del lote
            );

            -- Opción para evitar sobrecarga en bases de datos grandes (pausa opcional)
            PERFORM pg_sleep(0.1);

        EXCEPTION
            WHEN OTHERS THEN
                RAISE WARNING 'Error en el lote %: %', current_index + 1, SQLERRM;
        END;
    END LOOP;

    RAISE NOTICE 'Migración completada!';
END;
$function$;

CREATE OR REPLACE FUNCTION public.migrate_existing_transactions_to_reports(p_start_index integer DEFAULT 0, p_max_records integer DEFAULT '-1'::integer)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    cur CURSOR FOR 
        SELECT transaction_id, creation_date_time, host_type, server_name, time_zone,
               terminal_serial_number, merchant_type, merchant_type_code, transaction_type,
               transaction_amount_bd, transaction_currency_code_number, transaction_status
        FROM public.transaction_data
        ORDER BY transaction_id
        OFFSET p_start_index LIMIT CASE WHEN p_max_records = -1 THEN NULL ELSE p_max_records END
        FOR UPDATE SKIP LOCKED; -- Evita bloqueos si hay múltiples procesos ejecutando la función

    r RECORD;
    is_approved BOOLEAN;
    counter INT := 0;
BEGIN
    -- Abrir el cursor con el rango especificado
    OPEN cur;

    LOOP
        -- Obtener la siguiente fila
        FETCH cur INTO r;
        EXIT WHEN NOT FOUND; -- Salir si no hay más registros

        -- Determinar si la transacción fue aprobada
        is_approved := (r.transaction_status = 'Approved');

        -- Mostrar la consulta SQL exacta que se ejecutará
        RAISE NOTICE 'SQL: SELECT save_transaction_reports(
            ''%'', -- creation_date_time
            ''%'', -- host_type
            ''%'', -- server_name
            ''%'', -- time_zone
            ''%'', -- terminal_serial_number
            ''%'', -- merchant_type
            ''%'', -- merchant_type_code
            ''%'', -- transaction_type
            %, -- transaction_amount_bd
            %, -- transaction_currency_code_number
            %, -- transaction_count
            %  -- is_approved
        );',
            r.creation_date_time, 
            r.host_type, 
            r.server_name, 
            r.time_zone, 
            r.terminal_serial_number, 
            r.merchant_type, 
            r.merchant_type_code, 
            r.transaction_type, 
            r.transaction_amount_bd, 
            r.transaction_currency_code_number,
            1, -- Siempre 1 por registro
            is_approved;

        BEGIN
            -- Llamar a save_transaction_reports
            PERFORM save_transaction_reports(
                r.creation_date_time,  
                r.host_type,           
                r.server_name,         
                r.time_zone,           
                r.terminal_serial_number, 
                r.merchant_type,       
                r.merchant_type_code,  
                r.transaction_type,    
                r.transaction_amount_bd, 
                r.transaction_currency_code_number,
                1,                      -- Cantidad de transacciones
                is_approved             -- Si la transacción fue aprobada
            );

            -- Incrementar contador y mostrar progreso cada 500 registros
            counter := counter + 1;
            IF counter % 500 = 0 THEN
                RAISE NOTICE 'Procesados: % registros...', counter;
            END IF;

        EXCEPTION
            WHEN OTHERS THEN
                -- Capturar errores sin interrumpir la ejecución
                RAISE WARNING 'Error procesando transaction_id %: %', r.transaction_id, SQLERRM;
        END;

    END LOOP;

    -- Cerrar cursor
    CLOSE cur;
END;
$function$;

CREATE OR REPLACE FUNCTION public.save_transaction_report(p_host_type character varying, p_server_name text, p_time_zone character varying, p_terminal_serial_number character varying, p_merchant_type character varying, p_merchant_type_code character varying, p_transaction_type character varying, p_transaction_amount numeric, p_transaction_currency_code_number integer, p_transaction_count bigint, p_is_approved boolean, p_report_type text, p_transaction_status character varying, p_ram_total_pm bigint, p_ram_used_pm bigint, p_ram_usage_percentage_pm numeric, p_storage_total_pm bigint, p_storage_used_pm bigint, p_storage_usage_percentage_pm numeric, p_network_usage_percentage_pm numeric, p_cpu_usage_percentage_pm numeric)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE 
    period TEXT;
    periods TEXT[] := ARRAY['Daily', 'Weekly', 'Monthly', 'Quarterly', 'Semiannual', 'Annual'];
    is_credit BOOLEAN;
    existing_id BIGINT;
    start_date TIMESTAMP WITH TIME ZONE;
    end_date TIMESTAMP WITH TIME ZONE;
    p_transaction_currency_symbol TEXT := '';
    p_transaction_currency_code TEXT := '';
    p_total_txns_commerr_count BIGINT := 0;
    p_total_txns_rejected_pm BIGINT := 0;
    p_total_txns_approved_pm BIGINT := 0;
    p_total_transaction_commerr_pm BIGINT := 0;
    p_connection_count_pm INT := 0;
    is_commerr BOOLEAN := FALSE;
    current_minute TIMESTAMP WITH TIME ZONE;
     p_transaction_date_time TIMESTAMP WITH TIME ZONE=NOW();

BEGIN
    RAISE NOTICE 'Iniciando función save_transaction_report con los siguientes parámetros:';
    RAISE NOTICE 'p_host_type: %', p_host_type;
    RAISE NOTICE 'p_server_name: %', p_server_name;
    RAISE NOTICE 'p_time_zone: %', p_time_zone;
    RAISE NOTICE 'p_terminal_serial_number: %', p_terminal_serial_number;
    RAISE NOTICE 'p_merchant_type: %', p_merchant_type;
    RAISE NOTICE 'p_merchant_type_code: %', p_merchant_type_code;
    RAISE NOTICE 'p_transaction_type: %', p_transaction_type;
    RAISE NOTICE 'p_transaction_amount: %', p_transaction_amount;
    RAISE NOTICE 'p_transaction_currency_code_number: %', p_transaction_currency_code_number;
    RAISE NOTICE 'p_transaction_count: %', p_transaction_count;
    RAISE NOTICE 'p_is_approved: %', p_is_approved;
    RAISE NOTICE 'p_report_type: %', p_report_type;
    RAISE NOTICE 'p_transaction_status: %', p_transaction_status;
    RAISE NOTICE 'p_ram_total_pm: %', p_ram_total_pm;
    RAISE NOTICE 'p_ram_used_pm: %', p_ram_used_pm;
    RAISE NOTICE 'p_ram_usage_percentage_pm: %', p_ram_usage_percentage_pm;
    RAISE NOTICE 'p_storage_total_pm: %', p_storage_total_pm;
    RAISE NOTICE 'p_storage_used_pm: %', p_storage_used_pm;
    RAISE NOTICE 'p_storage_usage_percentage_pm: %', p_storage_usage_percentage_pm;
    RAISE NOTICE 'p_network_usage_percentage_pm: %', p_network_usage_percentage_pm;
    RAISE NOTICE 'p_cpu_usage_percentage_pm: %', p_cpu_usage_percentage_pm;
   -- Obtener el número de conexiones activas
    SELECT COUNT(*) INTO p_connection_count_pm
    FROM pg_stat_activity
    WHERE state = 'active'; 


    -- Determinar si la transacción es de crédito o débito (suma o resta)
    is_credit := p_transaction_type IN ('Sale', 'SaleWithTip', 'SalePosTip', 'CheckIn', 'RentalIn', 'Deposit');
    RAISE NOTICE 'is_credit: %', is_credit;

    -- Verificar si hubo un error de comunicación
    is_commerr := LOWER(p_transaction_status) LIKE '%host%' OR LOWER(p_transaction_status) LIKE '%timeout%';
    RAISE NOTICE 'is_commerr: %', is_commerr;

    -- Obtener el símbolo y código de la moneda
    SELECT 
        COALESCE(currency_code, '') AS currency_code,
        COALESCE(symbol, '') AS symbol
    INTO 
        p_transaction_currency_code, 
        p_transaction_currency_symbol
    FROM 
        currency_symbols
    WHERE 
        numeric_code = p_transaction_currency_code_number;
    RAISE NOTICE 'p_transaction_currency_code: %', p_transaction_currency_code;
    RAISE NOTICE 'p_transaction_currency_symbol: %', p_transaction_currency_symbol;

    -- Obtener el minuto actual para el reinicio de contadores por minuto
    current_minute := date_trunc('minute', p_transaction_date_time);
    RAISE NOTICE 'current_minute: %', current_minute;

    -- Insertar o actualizar reportes para cada período
    FOREACH period IN ARRAY periods LOOP
        RAISE NOTICE 'Procesando período: %', period;

        -- Calcular fechas de inicio y fin según el período
        CASE period
            WHEN 'Daily' THEN  
                start_date := date_trunc('day', p_transaction_date_time);
                end_date := start_date + interval '1 day' - interval '1 second';

            WHEN 'Weekly' THEN 
                start_date := date_trunc('week', p_transaction_date_time) + interval '1 day';  -- Lunes como inicio de semana
                end_date := start_date + interval '6 days' + interval '23 hours 59 minutes 59 seconds';

            WHEN 'Monthly' THEN 
                start_date := date_trunc('month', p_transaction_date_time);
                end_date := (date_trunc('month', p_transaction_date_time) + interval '1 month') - interval '1 second';

            WHEN 'Quarterly' THEN 
                start_date := date_trunc('quarter', p_transaction_date_time);
                end_date := (date_trunc('quarter', p_transaction_date_time) + interval '3 months') - interval '1 second';

            WHEN 'Semiannual' THEN 
                IF EXTRACT(MONTH FROM p_transaction_date_time) <= 6 THEN
                    start_date := make_timestamp(EXTRACT(YEAR FROM p_transaction_date_time)::int, 1, 1, 0, 0, 0);
                    end_date := make_timestamp(EXTRACT(YEAR FROM p_transaction_date_time)::int, 6, 30, 23, 59, 59);
                ELSE
                    start_date := make_timestamp(EXTRACT(YEAR FROM p_transaction_date_time)::int, 7, 1, 0, 0, 0);
                    end_date := make_timestamp(EXTRACT(YEAR FROM p_transaction_date_time)::int, 12, 31, 23, 59, 59);
                END IF;

            WHEN 'Annual' THEN  
                start_date := date_trunc('year', p_transaction_date_time);
                end_date := (date_trunc('year', p_transaction_date_time) + interval '1 year') - interval '1 second';
        END CASE;

        RAISE NOTICE 'start_date: %', start_date;
        RAISE NOTICE 'end_date: %', end_date;

        -- Verificar si ya existe un registro para este período y nivel
        SELECT t.transactions_meta_data_id INTO existing_id
        FROM transactions_meta_data t
        WHERE t.report_time_period = period
          AND t.date_time_initial = start_date
          AND t.date_time_end = end_date 
          AND t.server_name = p_server_name 
          AND t.transaction_currency_code_number = p_transaction_currency_code_number
          AND COALESCE(t.terminal_serial_number, '') = COALESCE(p_terminal_serial_number, '')
          AND COALESCE(t.merchant_type, '') = COALESCE(p_merchant_type, '')
          AND COALESCE(t.merchant_type_code, '') = COALESCE(p_merchant_type_code, '')
          AND t.report_type = p_report_type;

        RAISE NOTICE 'existing_id: %', existing_id;

        -- Si existe, actualizar los valores
        IF existing_id IS NOT NULL THEN
            RAISE NOTICE 'Actualizando registro existente para período: %', period;
            UPDATE transactions_meta_data
            SET 
                total_transaction_approved_amount = total_transaction_approved_amount + 
                    CASE 
                        WHEN p_is_approved AND is_credit THEN p_transaction_amount 
                        WHEN p_is_approved AND NOT is_credit THEN -p_transaction_amount
                        ELSE 0
                    END,
                total_transaction_approved_count = total_transaction_approved_count + 
                    CASE 
                        WHEN p_is_approved AND is_credit THEN p_transaction_count 
                        WHEN p_is_approved AND NOT is_credit THEN -p_transaction_count
                        ELSE 0
                    END,
                total_transaction_rejected_amount = total_transaction_rejected_amount + 
                    CASE 
                        WHEN NOT p_is_approved AND is_credit THEN p_transaction_amount 
                        WHEN NOT p_is_approved AND NOT is_credit THEN -p_transaction_amount
                        ELSE 0
                    END,
                total_transaction_rejected_count = total_transaction_rejected_count + 
                    CASE 
                        WHEN NOT p_is_approved AND is_credit THEN p_transaction_count 
                        WHEN NOT p_is_approved AND NOT is_credit THEN -p_transaction_count
                        ELSE 0
                    END,
                total_txns_commerr_count = total_txns_commerr_count + 
                    CASE 
                        WHEN is_commerr THEN 1 
                        ELSE 0
                    END,
                ram_total_pm = p_ram_total_pm,
                ram_used_pm = p_ram_used_pm,
                connection_count_pm=p_connection_count_pm,
                ram_usage_percentage_pm = p_ram_usage_percentage_pm,
                storage_total_pm = p_storage_total_pm,
                storage_used_pm = p_storage_used_pm,
                storage_usage_percentage_pm = p_storage_usage_percentage_pm,
                network_usage_percentage_pm = p_network_usage_percentage_pm,
                cpu_usage_percentage_pm = p_cpu_usage_percentage_pm,
                last_modification_date_time = NOW(),
                -- Reiniciar contadores por minuto si ha pasado más de un minuto desde la última actualización
                total_txns_approved_pm = CASE 
                                            WHEN last_update_pm_date_time < current_minute THEN total_txns_approved_pm
                                            ELSE total_txns_approved_pm + 
                                                        CASE 
                                                            WHEN p_is_approved THEN 1 
                                                            ELSE 0
                                                        END
                                        END,
                total_txns_rejected_pm = CASE 
                                            WHEN last_update_pm_date_time < current_minute THEN total_txns_rejected_pm
                                            ELSE total_txns_rejected_pm + 
                                                        CASE 
                                                            WHEN p_is_approved THEN 0
                                                            ELSE 1
                                                        END
                                        END,
                total_transaction_commerr_pm = CASE 
                                                  WHEN last_update_pm_date_time < current_minute THEN total_transaction_commerr_pm
                                                  ELSE total_transaction_commerr_pm +
                                                        CASE 
                                                            WHEN is_commerr THEN 1 
                                                            ELSE 0
                                                        END
                                              END,
                last_update_pm_date_time = CASE 
                                              WHEN last_update_pm_date_time < current_minute THEN current_minute
                                              ELSE last_update_pm_date_time
                                          END
            WHERE transactions_meta_data_id = existing_id;
        ELSE
            -- Si no existe, insertar un nuevo registro
            RAISE NOTICE 'Insertando nuevo registro para período: %', period;
            INSERT INTO transactions_meta_data (
                date_time_initial,
                date_time_end,
                report_time_period,
                host_type,
                server_name,
                time_zone,
                terminal_serial_number,
                merchant_type,
                merchant_type_code,
                transaction_type,
                report_type,
                total_transaction_approved_amount,
                total_transaction_approved_count,
                total_transaction_rejected_amount,
                total_transaction_rejected_count,
                creation_date_time,
                last_modification_date_time,
                transaction_currency_code,
                transaction_currency_symbol,
                transaction_currency_code_number,
                total_txns_commerr_count,
                ram_total_pm,
                ram_used_pm,
                ram_usage_percentage_pm,
                storage_total_pm,
                storage_used_pm,
                storage_usage_percentage_pm,
                network_usage_percentage_pm,
                cpu_usage_percentage_pm,
                total_txns_approved_pm,
                total_txns_rejected_pm,
                total_transaction_commerr_pm,
                last_update_pm_date_time,
                connection_count_pm
            ) VALUES (
                start_date,
                end_date,
                period,
                p_host_type,
                p_server_name,
                p_time_zone,
                p_terminal_serial_number,
                p_merchant_type,
                p_merchant_type_code,
                p_transaction_type,
                p_report_type,
                CASE  --total_transaction_approved_amount
                    WHEN p_is_approved AND is_credit THEN p_transaction_amount 
                    WHEN p_is_approved AND NOT is_credit THEN -p_transaction_amount
                    ELSE 0
                END,
                CASE 
                    WHEN p_is_approved THEN 1
                    ELSE 0
                END,
                CASE 
                    WHEN NOT p_is_approved AND is_credit THEN p_transaction_amount 
                    WHEN NOT p_is_approved AND NOT is_credit THEN -p_transaction_amount
                    ELSE 0
                END,
                CASE 
                    WHEN p_is_approved THEN 0
                    ELSE 1
                END,
                NOW(),
                NOW(),
                p_transaction_currency_code,
                p_transaction_currency_symbol,
                p_transaction_currency_code_number,
                CASE 
                    WHEN is_commerr THEN 1 
                    ELSE 0
                END,
                p_ram_total_pm,
                p_ram_used_pm,
                p_ram_usage_percentage_pm,
                p_storage_total_pm,
                p_storage_used_pm, 
                p_storage_usage_percentage_pm,
                p_network_usage_percentage_pm,
                p_cpu_usage_percentage_pm,
                CASE 
                    WHEN p_is_approved THEN 1
                    ELSE 0
                END,  -- Inicializar contadores por minuto
                CASE 
                    WHEN p_is_approved THEN 0
                    ELSE 1
                END,
                CASE 
                    WHEN is_commerr THEN 1 
                    ELSE 0
                END,
                current_minute,
                p_connection_count_pm
            );
        END IF;
    END LOOP;
END;
$function$;

CREATE OR REPLACE FUNCTION public.save_transaction_reports(p_host_type character varying, p_server_name text, p_time_zone character varying, p_terminal_serial_number character varying, p_merchant_type character varying, p_merchant_type_code character varying, p_transaction_type character varying, p_transaction_amount numeric, p_transaction_currency_code_number integer, p_transaction_count bigint, p_is_approved boolean, p_transaction_status character varying, p_ram_total_pm bigint, p_ram_used_pm bigint, p_ram_usage_percentage_pm numeric, p_storage_total_pm bigint, p_storage_used_pm bigint, p_storage_usage_percentage_pm numeric, p_network_usage_percentage_pm numeric, p_cpu_usage_percentage_pm numeric)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    -- 1. Reporte por Terminal (si el terminal_serial_number está presente)
    IF p_terminal_serial_number IS NOT NULL THEN
        PERFORM save_transaction_report(
            p_host_type, 
            p_server_name, 
            p_time_zone,
            p_terminal_serial_number,
            p_merchant_type,
            p_merchant_type_code,
            p_transaction_type,
            p_transaction_amount,
            p_transaction_currency_code_number,
            p_transaction_count,
            p_is_approved,
            'Terminal' ,
             
            p_transaction_status ,
            p_ram_total_pm ,
            p_ram_used_pm ,
            p_ram_usage_percentage_pm,
            p_storage_total_pm ,
            p_storage_used_pm ,
            p_storage_usage_percentage_pm ,
            p_network_usage_percentage_pm ,
            p_cpu_usage_percentage_pm  
        );
    END IF;

    -- 2. Reporte por Comercio (si merchant_type y merchant_code están presentes)
    IF p_merchant_type IS NOT NULL AND p_merchant_type_code IS NOT NULL THEN
        PERFORM save_transaction_report(
            p_host_type,
            p_server_name,
            p_time_zone,
            NULL,  -- No especificamos terminal para este nivel
            p_merchant_type,
            p_merchant_type_code,
            p_transaction_type,
            p_transaction_amount,
            p_transaction_currency_code_number,
            p_transaction_count, 
            p_is_approved,
            'Merchant',
             p_transaction_status ,
            p_ram_total_pm ,
            p_ram_used_pm ,
            p_ram_usage_percentage_pm,
            p_storage_total_pm ,
            p_storage_used_pm ,
            p_storage_usage_percentage_pm ,
            p_network_usage_percentage_pm ,
            p_cpu_usage_percentage_pm  
        );
    END IF;

    -- 3. Reporte por Tipo de Transacción (independiente de terminal o comercio)
    PERFORM save_transaction_report(
        p_host_type,
        p_server_name,
        p_time_zone,
        NULL, -- No se asocia a un terminal
        NULL, -- No se asocia a un merchant
        NULL, -- No se asocia a un merchant_code
        p_transaction_type,
        p_transaction_amount,
         p_transaction_currency_code_number,

        p_transaction_count,
        p_is_approved,
        'TransactionType',
         p_transaction_status ,
            p_ram_total_pm ,
            p_ram_used_pm ,
            p_ram_usage_percentage_pm,
            p_storage_total_pm ,
            p_storage_used_pm ,
            p_storage_usage_percentage_pm ,
            p_network_usage_percentage_pm ,
            p_cpu_usage_percentage_pm   
    );

    -- 4. Reporte Universal (sin terminal, sin comercio, solo resumen global)
    PERFORM save_transaction_report(
        p_host_type,
        p_server_name,
        p_time_zone,
        NULL, -- No se asocia a un terminal
        NULL, -- No se asocia a un merchant
        NULL, -- No se asocia a un merchant_code
        NULL, -- No se asocia a una transacción específica
        p_transaction_amount,
            p_transaction_currency_code_number,

        p_transaction_count,
        p_is_approved,
        'Universal',
         p_transaction_status ,
            p_ram_total_pm ,
            p_ram_used_pm ,
            p_ram_usage_percentage_pm,
            p_storage_total_pm ,
            p_storage_used_pm ,
            p_storage_usage_percentage_pm ,
            p_network_usage_percentage_pm ,
            p_cpu_usage_percentage_pm  
    );

END;
$function$;

CREATE OR REPLACE FUNCTION public.transaction_report(p_affiliation text DEFAULT NULL::text, p_terminal_serial_number text DEFAULT NULL::text)
 RETURNS TABLE(v_uuid bytea, v_transaction_id bigint, v_authorization_id_response character varying, v_card_type character varying, v_client_email character varying, v_clientip character varying, v_client_location character varying, v_client_user_name character varying, v_creation_date_time timestamp without time zone, v_db_kcv text, v_device_model character varying, v_last_modification_date_time timestamp without time zone, v_merchant_type character varying, v_original_transaction_id bigint, v_rrn character varying, v_server_name text, v_terminal_serial_number character varying, v_transaction_amount character varying, v_transaction_type character varying, v_creation_timestamp timestamp without time zone, v_fiid character varying, v_affiliation character varying, v_bank_name character varying, v_merchant_address character varying, v_merchant_name character varying, v_merchant_type_code character varying, v_card_number character varying, v_error_code character varying, v_error_message character varying)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
     uuid,
              transaction_id AS v_transaction_id, 
        authorization_id_response AS v_authorization_id_response, 
        card_type AS v_card_type, 
        client_email AS v_client_email, 
        clientip AS v_clientip, 
        client_location AS v_client_location, 
        client_user_name AS v_client_user_name, 
        creation_date_time AS v_creation_date_time, 
        db_kcv AS v_db_kcv, 
        device_model AS v_device_model, 
        last_modification_date_time AS v_last_modification_date_time, 
        merchant_type AS v_merchant_type, 
        original_transaction_id AS v_original_transaction_id, 
        rrn AS v_rrn, 
        server_name AS v_server_name, 
        terminal_serial_number AS v_terminal_serial_number, 
        transaction_amount AS v_transaction_amount, 
        transaction_type AS v_transaction_type,  
        creation_timestamp AS v_creation_timestamp, 
        fiid AS v_fiid, 
        affiliation AS v_affiliation, 
        bank_name AS v_bank_name, 
        merchant_address AS v_merchant_address, 
        merchant_name AS v_merchant_name, 
        merchant_type_code AS v_merchant_type_code, 
        card_number AS v_card_number,
           error_code AS v_error_code,
           error_message AS v_error_message


    FROM 
        public.transaction_data 
    WHERE 
        (p_affiliation IS NULL OR affiliation = p_affiliation) AND  -- Filtro de afiliación
        (p_terminal_serial_number IS NULL OR terminal_serial_number = p_terminal_serial_number)  -- Filtro de terminal_serial_number
    ORDER BY 
        creation_date_time DESC;  -- Ordenar de forma descendente
END;
$function$;

CREATE OR REPLACE FUNCTION public.transaction_report2(p_terminal_serial_number text DEFAULT NULL::text)
 RETURNS TABLE(v_uuid bytea, v_transaction_id bigint, v_authorization_id_response character varying, v_card_type character varying, v_client_email character varying, v_clientip character varying, v_client_location character varying, v_client_user_name character varying, v_creation_date_time timestamp without time zone, v_db_kcv text, v_device_model character varying, v_last_modification_date_time timestamp without time zone, v_merchant_type character varying, v_original_transaction_id bigint, v_rrn character varying, v_server_name text, v_terminal_serial_number character varying, v_transaction_amount character varying, v_transaction_type character varying, v_creation_timestamp timestamp without time zone, v_fiid character varying, v_affiliation character varying, v_bank_name character varying, v_merchant_address character varying, v_merchant_name character varying, v_merchant_type_code character varying, v_card_number character varying, v_error_code character varying, v_error_message character varying)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
     uuid,
              transaction_id AS v_transaction_id, 
        authorization_id_response AS v_authorization_id_response, 
        card_type AS v_card_type, 
        client_email AS v_client_email, 
        clientip AS v_clientip, 
        client_location AS v_client_location, 
        client_user_name AS v_client_user_name, 
        creation_date_time AS v_creation_date_time, 
        db_kcv AS v_db_kcv, 
        device_model AS v_device_model, 
        last_modification_date_time AS v_last_modification_date_time, 
        merchant_type AS v_merchant_type, 
        original_transaction_id AS v_original_transaction_id, 
        rrn AS v_rrn, 
        server_name AS v_server_name, 
        terminal_serial_number AS v_terminal_serial_number, 
        transaction_amount AS v_transaction_amount, 
        transaction_type AS v_transaction_type,  
        creation_timestamp AS v_creation_timestamp, 
        fiid AS v_fiid, 
        affiliation AS v_affiliation, 
        bank_name AS v_bank_name, 
        merchant_address AS v_merchant_address, 
        merchant_name AS v_merchant_name, 
        merchant_type_code AS v_merchant_type_code, 
        card_number AS v_card_number,
        
           error_code AS v_error_code,
           error_message AS v_error_message


    FROM 
        public.transaction_data 
    WHERE 
        (p_terminal_serial_number IS NULL OR terminal_serial_number = p_terminal_serial_number)  -- Filtro de terminal_serial_number
    ORDER BY 
        creation_date_time DESC;  -- Ordenar de forma descendente
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_iso_request_by_uuid(p_uuid bytea, p_rrn character varying, p_terminal_serial_number character varying, p_isoasciirequest bytea, p_time_zone character varying DEFAULT 'UTC'::character varying)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
  v_now_local_ts timestamp;  -- hora local según p_time_zone
  v_txn_id BIGINT;
BEGIN
  -- "now()" es timestamptz; lo convertimos a hora local (timestamp sin tz)
  v_now_local_ts := timezone(p_time_zone, now());

  UPDATE transaction_data
     SET isoasciirequest              = p_isoasciirequest,
     transaction_status='SentToHost',
         last_modification_date_time  = v_now_local_ts,
         time_zone                    = p_time_zone,
         operation_duration           = EXTRACT(EPOCH FROM (v_now_local_ts - creation_date_time))
   WHERE uuid = p_uuid  
   RETURNING transaction_id INTO v_txn_id;

  IF v_txn_id IS NULL THEN
    RETURN -1;
  END IF;

  RETURN v_txn_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_iso_request_by_uuid(p_uuid bytea, p_rrn character varying, p_terminal_serial_number character varying, p_system_trace_audit_number character varying, p_meta_data bytea, p_isoasciirequest bytea, p_time_zone character varying DEFAULT 'UTC'::character varying)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
  v_now_local_ts timestamp;  -- hora local según p_time_zone
  v_txn_id BIGINT;
BEGIN
  -- "now()" es timestamptz; lo convertimos a hora local (timestamp sin tz)
  v_now_local_ts := timezone(p_time_zone, now());

  UPDATE transaction_data
     SET isoasciirequest              = p_isoasciirequest,
     transaction_status='SentToHost',
         last_modification_date_time  = v_now_local_ts,
         time_zone                    = p_time_zone,
                   meta_data  = p_meta_data,

         operation_duration           = EXTRACT(EPOCH FROM (v_now_local_ts - creation_date_time))
   WHERE uuid = p_uuid  
   RETURNING transaction_id INTO v_txn_id;

  IF v_txn_id IS NULL THEN
    RETURN -1;
  END IF;

  RETURN v_txn_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_iso_request_by_uuid_full(p_uuid bytea, p_rrn character varying, p_terminal_serial_number character varying, p_system_trace_audit_number character varying, p_meta_data bytea, p_isoasciirequest bytea, p_time_zone character varying DEFAULT 'UTC'::character varying)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    v_now_local_ts TIMESTAMP;
    v_txn_id BIGINT;
BEGIN
    v_txn_id := 0;

    v_now_local_ts := timezone(p_time_zone, now());

    UPDATE transaction_data
       SET isoasciirequest             = p_isoasciirequest,
           transaction_status          = 'SentToHost',
           last_modification_date_time = v_now_local_ts,
           meta_data                   = p_meta_data,
           time_zone                   = p_time_zone,
           operation_duration          = EXTRACT(EPOCH FROM (v_now_local_ts - creation_date_time))
     WHERE uuid = p_uuid
       AND rrn = p_rrn
       AND terminal_serial_number = p_terminal_serial_number
       AND system_trace_audit_number = p_system_trace_audit_number;

    GET DIAGNOSTICS v_txn_id = ROW_COUNT;

    RETURN v_txn_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_iso_response_by_uuid(p_uuid bytea, p_rrn character varying, p_system_trace_audit_number character varying, p_terminal_serial_number character varying, p_isoasciiresponse bytea, p_time_zone character varying DEFAULT 'UTC'::character varying)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
  v_now_local_ts timestamp;  -- hora local según p_time_zone
  v_txn_id BIGINT;
BEGIN
  -- "now()" es timestamptz; lo convertimos a hora local (timestamp sin tz)
  v_now_local_ts := timezone(p_time_zone, now());

  UPDATE transaction_data
     SET isoasciiresponse              = p_isoasciiresponse,
     transaction_status='ResponseReceivedLately',
         last_modification_date_time  = v_now_local_ts,
         time_zone                    = p_time_zone,
         operation_duration           = EXTRACT(EPOCH FROM (v_now_local_ts - creation_date_time))
   WHERE  uuid = p_uuid  and transaction_status<>'Approved' or (system_trace_audit_number=p_system_trace_audit_number and transaction_status<>'Approved' and rrn=p_rrn and terminal_serial_number=p_terminal_serial_number )
   RETURNING transaction_id INTO v_txn_id;

  IF v_txn_id IS NULL THEN
    RETURN -1;
  END IF;

  RETURN v_txn_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_log_error_data(p_uuid bytea, p_error_code character varying DEFAULT 'Undefined'::character varying, p_error_code_number integer DEFAULT 0, p_error_message character varying DEFAULT ''::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_operation_duration double precision DEFAULT 0, p_rrn character varying DEFAULT ''::character varying, p_response_code character varying DEFAULT ''::character varying, p_card_bin character varying DEFAULT ''::character varying, p_card_number character varying DEFAULT ''::character varying, p_card_type character varying DEFAULT 'Undefined'::character varying, p_affiliation character varying DEFAULT ''::character varying, p_bank_name character varying DEFAULT ''::character varying, p_merchant_name character varying DEFAULT ''::character varying, p_merchant_type_code character varying DEFAULT ''::character varying, p_merchant_address character varying DEFAULT ''::character varying)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    v_log_id BIGINT;
BEGIN
    UPDATE public.log_meta_data
    SET 
         card_type = CASE WHEN p_card_type IS DISTINCT FROM 'Undefined' THEN p_card_type ELSE card_type END,
        response_code = CASE WHEN p_response_code IS DISTINCT FROM '' THEN p_response_code ELSE response_code END,

        error_code = CASE WHEN p_error_code IS DISTINCT FROM 'Undefined' THEN p_error_code ELSE error_code END,
        error_code_number = CASE WHEN p_error_code_number IS DISTINCT FROM 0 THEN p_error_code_number ELSE error_code_number END,
        error_message = CASE WHEN p_error_message IS DISTINCT FROM '' THEN p_error_message ELSE error_message END,
        operation_id = CASE WHEN p_operation_id IS DISTINCT FROM 'Undefined' THEN p_operation_id ELSE operation_id END,
        operation_duration = CASE WHEN p_operation_duration IS DISTINCT FROM 0 THEN p_operation_duration ELSE operation_duration END,
        rrn = CASE WHEN p_rrn <> '' THEN p_rrn ELSE rrn END,
        card_bin = CASE WHEN p_card_bin <> '' THEN p_card_bin ELSE card_bin END,
        card_number = CASE WHEN p_card_number <> '' THEN p_card_number ELSE card_number END,
        affiliation = CASE WHEN p_affiliation <> '' THEN p_affiliation ELSE affiliation END,
        bank_name = CASE WHEN p_bank_name <> '' THEN p_bank_name ELSE bank_name END,
        merchant_name = CASE WHEN p_merchant_name <> '' THEN p_merchant_name ELSE merchant_name END,
        merchant_type_code = CASE WHEN p_merchant_type_code <> '' THEN p_merchant_type_code ELSE merchant_type_code END,
        merchant_address = CASE WHEN p_merchant_address <> '' THEN p_merchant_address ELSE merchant_address END,
        last_modification_date_time = CURRENT_TIMESTAMP
    WHERE uuid = p_uuid
    RETURNING log_id INTO v_log_id;

    RETURN v_log_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_reverse_data(p_rrn character varying, p_result_message text DEFAULT ''::text, p_iso_reverse_response text DEFAULT ''::text, p_sender text DEFAULT 'host'::text)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$ 
BEGIN
    -- Actualizar los campos de la tabla reverses_data según el rrn proporcionado
    UPDATE reverses_data
    SET
        last_modification_date_time = NOW(), -- Usar la fecha y hora actual
        result_message = p_result_message,
        sender=p_sender,
        iso_reverse_response = p_iso_reverse_response
    WHERE
        rrn = p_rrn;

    -- Opcional: Registra un mensaje de éxito si se actualizó alguna fila
    IF FOUND THEN
        RAISE NOTICE 'Registro actualizado para rrn: %', p_rrn;
    ELSE
        RAISE WARNING 'No se encontró registro para rrn: %', p_rrn;
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_reverse_status(p_transaction_id bigint DEFAULT NULL::bigint, p_reverse_status text DEFAULT ''::text)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$ 
BEGIN
    -- Actualizar los campos de la tabla reverses_data según el rrn proporcionado
    UPDATE reverses_data
    SET
        last_modification_date_time = NOW(), -- Usar la fecha y hora actual
        reverse_status = p_reverse_status
    WHERE
        transaction_id = p_transaction_id;

    -- Opcional: Registra un mensaje de éxito si se actualizó alguna fila
    IF FOUND THEN
        RAISE NOTICE 'Registro actualizado para p_transaction_id: %', p_transaction_id;
    ELSE
        RAISE WARNING 'No se encontró registro para p_transaction_id: %', p_transaction_id;
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_transaction_status(p_transaction_id bigint DEFAULT 0, p_transaction_status text DEFAULT ''::text)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    -- Actualizar los campos de la tabla transaction_data según el rrn proporcionado
    UPDATE transaction_data
    SET
        last_modification_date_time = NOW(), -- Usar la fecha y hora actual
        transaction_status = p_transaction_status
    WHERE
       transaction_id = p_transaction_id ;

    -- Opcional: Registra un mensaje de éxito si se actualizó alguna fila
    IF FOUND THEN 
        RAISE NOTICE 'Registro actualizado para p_transaction_id: % ', p_transaction_id;
           RETURN p_transaction_id;

    ELSE
        RAISE WARNING 'No se encontró registro para p_transaction_id: %', p_transaction_id;
           RETURN -1;

    END IF;


END;
$function$;

CREATE OR REPLACE FUNCTION public.upsert_client_activity_data(p_account_locked boolean, p_account_suspicious boolean, p_client_email character varying, p_client_geolocation character varying, p_clientip character varying, p_client_user_name character varying, p_clonned_requests integer, p_creation_date_time timestamp without time zone, p_device_model character varying, p_failed_login_attempts integer, p_frequent_location character varying, p_is_blocked boolean, p_last_login_date_time timestamp without time zone, p_last_modification_date_time timestamp without time zone, p_last_transaction_date_time timestamp without time zone, p_number_of_devices integer, p_number_of_transactions integer, p_reason_for_blocking character varying, p_registration_date_time timestamp without time zone, p_suspicious_activity_detected boolean, p_terminal_serial_number character varying, p_total_amount_spent double precision)
 RETURNS bigint
 LANGUAGE plpgsql
AS $function$
DECLARE
v_client_activity_id BIGINT;
BEGIN
INSERT INTO public.client_activity_data(
    account_locked, account_suspicious, client_email, client_geolocation, clientip,
    client_user_name, clonned_requests, creation_date_time, device_model, failed_login_attempts,
    frequent_location, is_blocked, last_login_date_time, last_modification_date_time,
    last_transaction_date_time, number_of_devices, number_of_transactions, reason_for_blocking,
    registration_date_time, suspicious_activity_detected, terminal_serial_number, total_amount_spent
) VALUES (
             p_account_locked, p_account_suspicious, p_client_email, p_client_geolocation, p_clientip,
             p_client_user_name, p_clonned_requests, p_creation_date_time, p_device_model, p_failed_login_attempts,
             p_frequent_location, p_is_blocked, p_last_login_date_time, p_last_modification_date_time,
             p_last_transaction_date_time, p_number_of_devices, p_number_of_transactions, p_reason_for_blocking,
             p_registration_date_time, p_suspicious_activity_detected, p_terminal_serial_number, p_total_amount_spent
         )
    ON CONFLICT (client_email, terminal_serial_number)
    DO UPDATE SET
    account_locked = EXCLUDED.account_locked,
               account_suspicious = EXCLUDED.account_suspicious,
               client_geolocation = EXCLUDED.client_geolocation,
               clientip = EXCLUDED.clientip,
               client_user_name = EXCLUDED.client_user_name,
               clonned_requests = EXCLUDED.clonned_requests,
               creation_date_time = EXCLUDED.creation_date_time,
               device_model = EXCLUDED.device_model,
               failed_login_attempts = EXCLUDED.failed_login_attempts,
               frequent_location = EXCLUDED.frequent_location,
               is_blocked = EXCLUDED.is_blocked,
               last_login_date_time = EXCLUDED.last_login_date_time,
               last_modification_date_time = EXCLUDED.last_modification_date_time,
               last_transaction_date_time = EXCLUDED.last_transaction_date_time,
               number_of_devices = EXCLUDED.number_of_devices,
               number_of_transactions = EXCLUDED.number_of_transactions,
               reason_for_blocking = EXCLUDED.reason_for_blocking,
               registration_date_time = EXCLUDED.registration_date_time,
               suspicious_activity_detected = EXCLUDED.suspicious_activity_detected,
               total_amount_spent = EXCLUDED.total_amount_spent
               RETURNING client_activity_id INTO v_client_activity_id;

RETURN v_client_activity_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.upsert_log_meta_data(p_bank character varying DEFAULT 'Undefined'::character varying, p_client_email character varying DEFAULT ''::character varying, p_client_geolocation character varying DEFAULT ''::character varying, p_clientip character varying DEFAULT ''::character varying, p_client_user_name character varying DEFAULT ''::character varying, p_creation_date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP, p_device_model character varying DEFAULT ''::character varying, p_error_code character varying DEFAULT 'Undefined'::character varying, p_error_code_number integer DEFAULT 0, p_error_message character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_last_modification_date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP, p_merchant_type character varying DEFAULT 'Undefined'::character varying, p_module_id character varying DEFAULT 'Undefined'::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_point_of_service_entry_mode character varying DEFAULT 'Undefined'::character varying, p_response_code character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_server_name character varying DEFAULT ''::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_transaction_amount character varying DEFAULT ''::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_uuid bytea DEFAULT '\x5c78'::bytea)
 RETURNS bigint
 LANGUAGE plpgsql
AS $function$
DECLARE
v_log_id bigint;
BEGIN
INSERT INTO public.log_meta_data(
    bank, client_email, client_geolocation, clientip, client_user_name,
    creation_date_time, device_model, error_code, error_code_number,
    error_message, host_type, last_modification_date_time, merchant_type,
    module_id, operation_id, point_of_service_entry_mode, response_code,
    rrn, server_name, terminal_serial_number, transaction_amount, transaction_type, uuid,operation_duration
) VALUES (
             p_bank, p_client_email, p_client_geolocation, p_clientip, p_client_user_name,
             p_creation_date_time, p_device_model, p_error_code, p_error_code_number,
             p_error_message, p_host_type, p_last_modification_date_time, p_merchant_type,
             p_module_id, p_operation_id, p_point_of_service_entry_mode, p_response_code,
             p_rrn, p_server_name, p_terminal_serial_number, p_transaction_amount, p_transaction_type, p_uuid,0
         )
    ON CONFLICT (uuid)
    DO UPDATE SET
    bank = CASE WHEN EXCLUDED.bank != 'Undefined' THEN EXCLUDED.bank ELSE log_meta_data.bank END,
               client_email = CASE WHEN EXCLUDED.client_email != '' THEN EXCLUDED.client_email ELSE log_meta_data.client_email END,
        client_geolocation = CASE WHEN EXCLUDED.client_geolocation != '' THEN EXCLUDED.client_geolocation ELSE log_meta_data.client_geolocation END,
        clientip = CASE WHEN EXCLUDED.clientip != '' THEN EXCLUDED.clientip ELSE log_meta_data.clientip END,
        client_user_name = CASE WHEN EXCLUDED.client_user_name != '' THEN EXCLUDED.client_user_name ELSE log_meta_data.client_user_name END,
        creation_date_time = CASE WHEN EXCLUDED.creation_date_time != CURRENT_TIMESTAMP THEN EXCLUDED.creation_date_time ELSE log_meta_data.creation_date_time END,
        device_model = CASE WHEN EXCLUDED.device_model != '' THEN EXCLUDED.device_model ELSE log_meta_data.device_model END,
        error_code = CASE WHEN EXCLUDED.error_code != 'Undefined' THEN EXCLUDED.error_code ELSE log_meta_data.error_code END,
        error_code_number = CASE WHEN EXCLUDED.error_code_number != 0 THEN EXCLUDED.error_code_number ELSE log_meta_data.error_code_number END,
        error_message = CASE WHEN EXCLUDED.error_message != '' THEN EXCLUDED.error_message ELSE log_meta_data.error_message END,
        host_type = CASE WHEN EXCLUDED.host_type != 'Undefined' THEN EXCLUDED.host_type ELSE log_meta_data.host_type END,
        last_modification_date_time = CASE WHEN EXCLUDED.last_modification_date_time != CURRENT_TIMESTAMP THEN EXCLUDED.last_modification_date_time ELSE log_meta_data.last_modification_date_time END,
        merchant_type = CASE WHEN EXCLUDED.merchant_type != 'Undefined' THEN EXCLUDED.merchant_type ELSE log_meta_data.merchant_type END,
        module_id = CASE WHEN EXCLUDED.module_id != 'Undefined' THEN EXCLUDED.module_id ELSE log_meta_data.module_id END,
        operation_id = CASE WHEN EXCLUDED.operation_id != 'Undefined' THEN EXCLUDED.operation_id ELSE log_meta_data.operation_id END,
        point_of_service_entry_mode = CASE WHEN EXCLUDED.point_of_service_entry_mode != 'Undefined' THEN EXCLUDED.point_of_service_entry_mode ELSE log_meta_data.point_of_service_entry_mode END,
        response_code = CASE WHEN EXCLUDED.response_code != '' THEN EXCLUDED.response_code ELSE log_meta_data.response_code END,
        rrn = CASE WHEN EXCLUDED.rrn != '' THEN EXCLUDED.rrn ELSE log_meta_data.rrn END,
        server_name = CASE WHEN EXCLUDED.server_name != '' THEN EXCLUDED.server_name ELSE log_meta_data.server_name END,
        terminal_serial_number = CASE WHEN EXCLUDED.terminal_serial_number != '' THEN EXCLUDED.terminal_serial_number ELSE log_meta_data.terminal_serial_number END,
        transaction_amount = CASE WHEN EXCLUDED.transaction_amount != '' THEN EXCLUDED.transaction_amount ELSE log_meta_data.transaction_amount END,
        transaction_type = CASE WHEN EXCLUDED.transaction_type != 'Undefined' THEN EXCLUDED.transaction_type ELSE log_meta_data.transaction_type END
    RETURNING log_id INTO v_log_id;

RETURN v_log_id;
END;
$function$;

CREATE OR REPLACE FUNCTION public.upsert_log_meta_data(p_time_zone character varying DEFAULT 'UTC'::character varying, p_bank character varying DEFAULT 'Undefined'::character varying, p_client_email character varying DEFAULT ''::character varying, p_client_geolocation character varying DEFAULT ''::character varying, p_clientip character varying DEFAULT ''::character varying, p_client_user_name character varying DEFAULT ''::character varying, p_creation_date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP, p_device_model character varying DEFAULT ''::character varying, p_error_code character varying DEFAULT 'Undefined'::character varying, p_error_code_number integer DEFAULT 0, p_error_message character varying DEFAULT ''::character varying, p_host_type character varying DEFAULT 'Undefined'::character varying, p_last_modification_date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP, p_merchant_type character varying DEFAULT 'Undefined'::character varying, p_module_id character varying DEFAULT 'Undefined'::character varying, p_operation_id character varying DEFAULT 'Undefined'::character varying, p_point_of_service_entry_mode character varying DEFAULT 'Undefined'::character varying, p_response_code character varying DEFAULT ''::character varying, p_rrn character varying DEFAULT ''::character varying, p_server_name character varying DEFAULT ''::character varying, p_terminal_serial_number character varying DEFAULT ''::character varying, p_transaction_amount character varying DEFAULT ''::character varying, p_transaction_type character varying DEFAULT 'Undefined'::character varying, p_uuid bytea DEFAULT '\x5c78'::bytea)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
v_log_id bigint;
BEGIN
INSERT INTO public.log_meta_data(time_zone,
    bank, client_email, client_geolocation, clientip, client_user_name,
    creation_date_time, device_model, error_code, error_code_number,
    error_message, host_type, last_modification_date_time, merchant_type,
    module_id, operation_id, point_of_service_entry_mode, response_code,
    rrn, server_name, terminal_serial_number, transaction_amount, transaction_type, uuid,operation_duration
) VALUES (p_time_zone,
             p_bank, p_client_email, p_client_geolocation, p_clientip, p_client_user_name,
             p_creation_date_time, p_device_model, p_error_code, p_error_code_number,
             p_error_message, p_host_type, p_last_modification_date_time, p_merchant_type,
             p_module_id, p_operation_id, p_point_of_service_entry_mode, p_response_code,
             p_rrn, p_server_name, p_terminal_serial_number, p_transaction_amount, p_transaction_type, p_uuid,0
         )
    ON CONFLICT (uuid)
    DO UPDATE SET
    bank = CASE WHEN EXCLUDED.bank != 'Undefined' THEN EXCLUDED.bank ELSE log_meta_data.bank END,
               client_email = CASE WHEN EXCLUDED.client_email != '' THEN EXCLUDED.client_email ELSE log_meta_data.client_email END,
        client_geolocation = CASE WHEN EXCLUDED.client_geolocation != '' THEN EXCLUDED.client_geolocation ELSE log_meta_data.client_geolocation END,
        clientip = CASE WHEN EXCLUDED.clientip != '' THEN EXCLUDED.clientip ELSE log_meta_data.clientip END,
        client_user_name = CASE WHEN EXCLUDED.client_user_name != '' THEN EXCLUDED.client_user_name ELSE log_meta_data.client_user_name END,
        creation_date_time = CASE WHEN EXCLUDED.creation_date_time != CURRENT_TIMESTAMP THEN EXCLUDED.creation_date_time ELSE log_meta_data.creation_date_time END,
        device_model = CASE WHEN EXCLUDED.device_model != '' THEN EXCLUDED.device_model ELSE log_meta_data.device_model END,
        error_code = CASE WHEN EXCLUDED.error_code != 'Undefined' THEN EXCLUDED.error_code ELSE log_meta_data.error_code END,
        error_code_number = CASE WHEN EXCLUDED.error_code_number != 0 THEN EXCLUDED.error_code_number ELSE log_meta_data.error_code_number END,
        error_message = CASE WHEN EXCLUDED.error_message != '' THEN EXCLUDED.error_message ELSE log_meta_data.error_message END,
        host_type = CASE WHEN EXCLUDED.host_type != 'Undefined' THEN EXCLUDED.host_type ELSE log_meta_data.host_type END,
        last_modification_date_time = CASE WHEN EXCLUDED.last_modification_date_time != CURRENT_TIMESTAMP THEN EXCLUDED.last_modification_date_time ELSE log_meta_data.last_modification_date_time END,
        merchant_type = CASE WHEN EXCLUDED.merchant_type != 'Undefined' THEN EXCLUDED.merchant_type ELSE log_meta_data.merchant_type END,
        module_id = CASE WHEN EXCLUDED.module_id != 'Undefined' THEN EXCLUDED.module_id ELSE log_meta_data.module_id END,
        operation_id = CASE WHEN EXCLUDED.operation_id != 'Undefined' THEN EXCLUDED.operation_id ELSE log_meta_data.operation_id END,
        point_of_service_entry_mode = CASE WHEN EXCLUDED.point_of_service_entry_mode != 'Undefined' THEN EXCLUDED.point_of_service_entry_mode ELSE log_meta_data.point_of_service_entry_mode END,
        response_code = CASE WHEN EXCLUDED.response_code != '' THEN EXCLUDED.response_code ELSE log_meta_data.response_code END,
        rrn = CASE WHEN EXCLUDED.rrn != '' THEN EXCLUDED.rrn ELSE log_meta_data.rrn END,
        server_name = CASE WHEN EXCLUDED.server_name != '' THEN EXCLUDED.server_name ELSE log_meta_data.server_name END,
        terminal_serial_number = CASE WHEN EXCLUDED.terminal_serial_number != '' THEN EXCLUDED.terminal_serial_number ELSE log_meta_data.terminal_serial_number END,
        transaction_amount = CASE WHEN EXCLUDED.transaction_amount != '' THEN EXCLUDED.transaction_amount ELSE log_meta_data.transaction_amount END,
        transaction_type = CASE WHEN EXCLUDED.transaction_type != 'Undefined' THEN EXCLUDED.transaction_type ELSE log_meta_data.transaction_type END
    RETURNING log_id INTO v_log_id;

RETURN v_log_id;
END;
$function$;

-- ============================================================================
-- 8. VIEWS
-- ============================================================================

CREATE OR REPLACE VIEW corepay_schema.transaction_data_public AS
SELECT creation_date_time,
    last_modification_date_time AS modification_date_time,
    terminal_serial_number,
    transaction_amount,
    transaction_type,
    card_number,
    affiliation,
    merchant_name,
    merchant_type_code,
    merchant_address,
    point_of_service_entry_mode,
    response_code,
    transaction_status,
    error_code,
    rrn,
    server_name,
    isoasciirequest,
    isoasciiresponse
   FROM transaction_data
  WHERE host_type::text = 'ProsaDUKPT'::text;

CREATE OR REPLACE VIEW public.transaction_data_public AS
SELECT creation_date_time,
    last_modification_date_time AS modification_date_time,
    terminal_serial_number,
    transaction_amount,
    transaction_type,
    card_number,
    affiliation,
    merchant_name,
    merchant_type_code,
    merchant_address,
    point_of_service_entry_mode,
    response_code,
    transaction_status,
    error_code,
    rrn,
    server_name,
    isoasciirequest,
    isoasciiresponse
   FROM transaction_data
  WHERE host_type::text = 'ProsaDUKPT'::text;

CREATE OR REPLACE VIEW public.viewtransactiondata AS
SELECT creation_date_time,
    last_modification_date_time AS modification_date_time,
    terminal_serial_number,
    transaction_amount,
    transaction_type,
    card_number,
    affiliation,
    merchant_name,
    merchant_type_code,
    merchant_address,
    point_of_service_entry_mode,
    response_code,
    transaction_status,
    error_code,
    rrn,
    server_name,
    isoasciirequest,
    isoasciiresponse
   FROM transaction_data
  WHERE host_type::text = 'ProsaDukpt'::text;

