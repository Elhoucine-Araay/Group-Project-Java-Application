--
-- PostgreSQL database dump
--


-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

-- Started on 2026-11-03 09:54:27

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_table_access_method = heap;

--
-- TOC entry 236 (class 1259 OID 17833)
-- Name: affectation; Type: TABLE; Schema: public; Owner: -
--

ALTER TABLE chantier
    ADD COLUMN budget   numeric(14,2) NOT NULL DEFAULT 0,
    ADD COLUMN depenses numeric(14,2) NOT NULL DEFAULT 0,
    ADD COLUMN image    varchar(150);

CREATE TABLE public.affectation (
    "idAffectation" integer NOT NULL,
    "dateAffectation" date,
    "dateFin" date,
    "typeRessource" character varying(100),
    statut character varying(100),
    "idChantier" integer NOT NULL,
    "idTache" integer NOT NULL,
    "idMateriel" integer,
    "idMachine" integer,
    "idOuvrier" integer
);


--
-- TOC entry 235 (class 1259 OID 17832)
-- Name: affectation_idAffectation_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.affectation ALTER COLUMN "idAffectation" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."affectation_idAffectation_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 222 (class 1259 OID 17782)
-- Name: chantier; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chantier (
    "idChantier" integer NOT NULL,
    nom character varying(100),
    adresse character varying(255),
    "dateDebut" date,
    "dateFinPrevue" date,
    statut character varying(100),
    avancement double precision,
    "idChefDepartement" integer NOT NULL,
    "idChefProjet" integer NOT NULL
);


--
-- TOC entry 221 (class 1259 OID 17781)
-- Name: chantier_idChantier_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.chantier ALTER COLUMN "idChantier" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."chantier_idChantier_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 237 (class 1259 OID 17839)
-- Name: chef_departement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chef_departement (
    "idUtilisateur" integer NOT NULL
);


--
-- TOC entry 238 (class 1259 OID 17850)
-- Name: chef_projet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chef_projet (
    "idUtilisateur" integer NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 17796)
-- Name: document; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.document (
    "idDocument" integer NOT NULL,
    nom character varying(100),
    type character varying(100),
    "dateCreation" date,
    "cheminFichier" character varying(255),
    description character varying(255),
    "idChantier" integer NOT NULL
);


--
-- TOC entry 225 (class 1259 OID 17795)
-- Name: document_idDocument_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.document ALTER COLUMN "idDocument" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."document_idDocument_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 230 (class 1259 OID 17812)
-- Name: machine; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.machine (
    "idMachine" integer NOT NULL,
    nom character varying(100),
    type character varying(100),
    etat character varying(100),
    "dateDerniereMaintenance" date,
    disponible boolean
);


--
-- TOC entry 229 (class 1259 OID 17811)
-- Name: machine_idMachine_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.machine ALTER COLUMN "idMachine" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."machine_idMachine_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 228 (class 1259 OID 17805)
-- Name: materiel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.materiel (
    "idMateriel" integer NOT NULL,
    nom character varying(100),
    description character varying(255),
    "quantiteStock" double precision,
    unite character varying(50),
    "seuilMinimum" double precision
);


--
-- TOC entry 227 (class 1259 OID 17804)
-- Name: materiel_idMateriel_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.materiel ALTER COLUMN "idMateriel" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."materiel_idMateriel_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 232 (class 1259 OID 17819)
-- Name: ouvrier; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ouvrier (
    "idOuvrier" integer NOT NULL,
    nom character varying(100),
    prenom character varying(100),
    specialite character varying(100),
    telephone character varying(20),
    "salaireJournalier" double precision
);


--
-- TOC entry 231 (class 1259 OID 17818)
-- Name: ouvrier_idOuvrier_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.ouvrier ALTER COLUMN "idOuvrier" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."ouvrier_idOuvrier_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 234 (class 1259 OID 17826)
-- Name: presence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.presence (
    "idPresence" integer NOT NULL,
    date date,
    "heureArrivee" character varying(20),
    statut character varying(100),
    observation character varying(255),
    "idOuvrier" integer NOT NULL
);


--
-- TOC entry 233 (class 1259 OID 17825)
-- Name: presence_idPresence_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.presence ALTER COLUMN "idPresence" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."presence_idPresence_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 224 (class 1259 OID 17789)
-- Name: tache; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tache (
    "idTache" integer NOT NULL,
    nom character varying(100),
    description character varying(255),
    "dateDebut" date,
    "dateFinPrevue" date,
    statut character varying(100),
    "pourcentageAvancement" double precision,
    "idChantier" integer NOT NULL
);


--
-- TOC entry 223 (class 1259 OID 17788)
-- Name: tache_idTache_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tache ALTER COLUMN "idTache" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public."tache_idTache_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 220 (class 1259 OID 17766)
-- Name: utilisateur; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.utilisateur (
    "idUtilisateur" integer CONSTRAINT utilisateur_id_utilisateur_not_null NOT NULL,
    nom character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    "motDePasse" character varying(255) CONSTRAINT utilisateur_mot_de_passe_hash_not_null NOT NULL
);


--
-- TOC entry 219 (class 1259 OID 17765)
-- Name: utilisateur_id_utilisateur_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.utilisateur ALTER COLUMN "idUtilisateur" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.utilisateur_id_utilisateur_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 4925 (class 2606 OID 17838)
-- Name: affectation affectation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT affectation_pkey PRIMARY KEY ("idAffectation");


--
-- TOC entry 4909 (class 2606 OID 17787)
-- Name: chantier chantier_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chantier
    ADD CONSTRAINT chantier_pkey PRIMARY KEY ("idChantier");


--
-- TOC entry 4927 (class 2606 OID 17844)
-- Name: chef_departement chef_departement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chef_departement
    ADD CONSTRAINT chef_departement_pkey PRIMARY KEY ("idUtilisateur");


--
-- TOC entry 4929 (class 2606 OID 17855)
-- Name: chef_projet chef_projet_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chef_projet
    ADD CONSTRAINT chef_projet_pkey PRIMARY KEY ("idUtilisateur");


--
-- TOC entry 4915 (class 2606 OID 17803)
-- Name: document document_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_pkey PRIMARY KEY ("idDocument");


--
-- TOC entry 4919 (class 2606 OID 17817)
-- Name: machine machine_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.machine
    ADD CONSTRAINT machine_pkey PRIMARY KEY ("idMachine");


--
-- TOC entry 4917 (class 2606 OID 17810)
-- Name: materiel materiel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materiel
    ADD CONSTRAINT materiel_pkey PRIMARY KEY ("idMateriel");


--
-- TOC entry 4921 (class 2606 OID 17824)
-- Name: ouvrier ouvrier_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ouvrier
    ADD CONSTRAINT ouvrier_pkey PRIMARY KEY ("idOuvrier");


--
-- TOC entry 4923 (class 2606 OID 17831)
-- Name: presence presence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.presence
    ADD CONSTRAINT presence_pkey PRIMARY KEY ("idPresence");


--
-- TOC entry 4913 (class 2606 OID 17794)
-- Name: tache tache_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tache
    ADD CONSTRAINT tache_pkey PRIMARY KEY ("idTache");


--
-- TOC entry 4911 (class 2606 OID 17869)
-- Name: chantier uq_chantier_chef_projet; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chantier
    ADD CONSTRAINT uq_chantier_chef_projet UNIQUE ("idChefProjet");


--
-- TOC entry 4905 (class 2606 OID 17780)
-- Name: utilisateur utilisateur_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_email_key UNIQUE (email);


--
-- TOC entry 4907 (class 2606 OID 17778)
-- Name: utilisateur utilisateur_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_pkey PRIMARY KEY ("idUtilisateur");


--
-- TOC entry 4935 (class 2606 OID 17888)
-- Name: affectation fk_affectation_chantier; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT fk_affectation_chantier FOREIGN KEY ("idChantier") REFERENCES public.chantier("idChantier");


--
-- TOC entry 4936 (class 2606 OID 17904)
-- Name: affectation fk_affectation_machine; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT fk_affectation_machine FOREIGN KEY ("idMachine") REFERENCES public.machine("idMachine");


--
-- TOC entry 4937 (class 2606 OID 17899)
-- Name: affectation fk_affectation_materiel; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT fk_affectation_materiel FOREIGN KEY ("idMateriel") REFERENCES public.materiel("idMateriel");


--
-- TOC entry 4938 (class 2606 OID 17909)
-- Name: affectation fk_affectation_ouvrier; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT fk_affectation_ouvrier FOREIGN KEY ("idOuvrier") REFERENCES public.ouvrier("idOuvrier");


--
-- TOC entry 4939 (class 2606 OID 17894)
-- Name: affectation fk_affectation_tache; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT fk_affectation_tache FOREIGN KEY ("idTache") REFERENCES public.tache("idTache");


--
-- TOC entry 4930 (class 2606 OID 17862)
-- Name: chantier fk_chantier_chef_departement; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chantier
    ADD CONSTRAINT fk_chantier_chef_departement FOREIGN KEY ("idChefDepartement") REFERENCES public.chef_departement("idUtilisateur");


--
-- TOC entry 4931 (class 2606 OID 17870)
-- Name: chantier fk_chantier_chef_projet; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chantier
    ADD CONSTRAINT fk_chantier_chef_projet FOREIGN KEY ("idChefProjet") REFERENCES public.chef_projet("idUtilisateur");


--
-- TOC entry 4940 (class 2606 OID 17845)
-- Name: chef_departement fk_chef_departement_utilisateur; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chef_departement
    ADD CONSTRAINT fk_chef_departement_utilisateur FOREIGN KEY ("idUtilisateur") REFERENCES public.utilisateur("idUtilisateur");


--
-- TOC entry 4941 (class 2606 OID 17856)
-- Name: chef_projet fk_chef_projet_utilisateur; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chef_projet
    ADD CONSTRAINT fk_chef_projet_utilisateur FOREIGN KEY ("idUtilisateur") REFERENCES public.utilisateur("idUtilisateur");


--
-- TOC entry 4933 (class 2606 OID 17882)
-- Name: document fk_document_chantier; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT fk_document_chantier FOREIGN KEY ("idChantier") REFERENCES public.chantier("idChantier");


--
-- TOC entry 4934 (class 2606 OID 17915)
-- Name: presence fk_presence_ouvrier; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.presence
    ADD CONSTRAINT fk_presence_ouvrier FOREIGN KEY ("idOuvrier") REFERENCES public.ouvrier("idOuvrier");


--
-- TOC entry 4932 (class 2606 OID 17876)
-- Name: tache fk_tache_chantier; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tache
    ADD CONSTRAINT fk_tache_chantier FOREIGN KEY ("idChantier") REFERENCES public.chantier("idChantier");


-- Completed on 2026-11-03 09:54:28

--
-- PostgreSQL database dump complete
--


