-- Migration: Add heading and source columns to course_chunk_embedding table
-- Run this SQL on your PostgreSQL vector database (learn_sphere_vector)

-- Add heading column (section heading context for the chunk)
ALTER TABLE course_chunk_embedding ADD COLUMN IF NOT EXISTS heading TEXT;

-- Add source column (origin: "markdown", "pdf", "docx", "txt", "manual")
ALTER TABLE course_chunk_embedding ADD COLUMN IF NOT EXISTS source VARCHAR(32) DEFAULT 'markdown';
