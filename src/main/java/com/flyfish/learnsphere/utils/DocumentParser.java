package com.flyfish.learnsphere.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility class for parsing uploaded documents (PDF / DOCX / TXT) into plain text.
 * @Author: FlyFish
 */
@Slf4j
public class DocumentParser {

    private DocumentParser() {}

    /**
     * Parse uploaded file to plain text.
     * Supported types: .pdf, .docx, .txt, .md
     */
    public static String parse(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null");
        }
        String lowerName = originalFilename.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            return parsePdf(file);
        } else if (lowerName.endsWith(".docx")) {
            return parseDocx(file);
        } else if (lowerName.endsWith(".txt") || lowerName.endsWith(".md")) {
            return parseTxt(file);
        } else {
            throw new IOException("Unsupported file type: " + originalFilename);
        }
    }

    /**
     * Detect source type from file extension
     */
    public static String detectSourceType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return "unknown";
        String lowerName = name.toLowerCase();
        if (lowerName.endsWith(".pdf")) return "pdf";
        if (lowerName.endsWith(".docx")) return "docx";
        if (lowerName.endsWith(".txt")) return "txt";
        if (lowerName.endsWith(".md")) return "markdown";
        return "unknown";
    }

    private static String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            log.info("Parsed PDF file: {}, length={}", file.getOriginalFilename(), text.length());
            return text;
        }
    }

    private static String parseDocx(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : paragraphs) {
                String text = p.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text.trim()).append("\n\n");
                }
            }
            log.info("Parsed DOCX file: {}, length={}", file.getOriginalFilename(), sb.length());
            return sb.toString();
        }
    }

    private static String parseTxt(MultipartFile file) throws IOException {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        log.info("Parsed TXT/MD file: {}, length={}", file.getOriginalFilename(), text.length());
        return text;
    }
}
