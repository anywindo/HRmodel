package service.payroll;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Image;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.payroll.PayrollItem;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PayslipPdfService {

    private final repository.company.CompanyProfileRepository companyProfileRepository;

    public PayslipPdfService(repository.company.CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    // Palette: Modern Corporate Navy & Slate
    private static final Color PRIMARY_NAVY = new Color(30, 58, 138);   // #1E3A8A
    private static final Color DARK_SLATE = new Color(15, 23, 42);     // #0F172A
    private static final Color TEXT_DARK = new Color(51, 65, 85);       // #334155
    private static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748B
    private static final Color BG_LIGHT = new Color(248, 250, 252);     // #F8FAFC
    private static final Color BG_HEADER = new Color(241, 245, 249);    // #F1F5F9
    private static final Color BORDER_COLOR = new Color(226, 232, 240); // #E2E8F0
    private static final Color ACCENT_BLUE = new Color(239, 246, 255);   // #EFF6FF
    private static final Color BLUE_BORDER = new Color(147, 197, 253);  // #93C5FD

    public byte[] generatePayslipPdf(PayrollItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Payroll item cannot be null");
        }

        // Fetch dynamic company profile
        model.company.CompanyProfile companyProfile = companyProfileRepository != null
                ? companyProfileRepository.findFirstByOrderByIdAsc().orElse(null)
                : null;

        String companyName = "GLOBAL HR ENTERPRISE";
        String companyAddress = "PT HR MODEL SYSTEM INDONESIA";
        Image logoImage = null;

        if (companyProfile != null && companyProfile.getDetail() != null) {
            if (companyProfile.getDetail().getCompanyName() != null && !companyProfile.getDetail().getCompanyName().isBlank()) {
                companyName = companyProfile.getDetail().getCompanyName().toUpperCase();
            }
            if (companyProfile.getDetail().getAddress() != null) {
                var addr = companyProfile.getDetail().getAddress();
                StringBuilder sb = new StringBuilder();
                if (addr.getStreet() != null && !addr.getStreet().isBlank()) sb.append(addr.getStreet());
                if (addr.getCity() != null && !addr.getCity().isBlank()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getCity());
                }
                if (addr.getCountry() != null && !addr.getCountry().isBlank()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getCountry());
                }
                if (sb.length() > 0) {
                    companyAddress = sb.toString();
                }
            }

            // Attempt to load logo image if available locally
            String logoUrl = companyProfile.getDetail().getLogoUrl();
            if (logoUrl != null && !logoUrl.isBlank()) {
                try {
                    if (logoUrl.contains("/uploads/")) {
                        String filename = logoUrl.substring(logoUrl.lastIndexOf("/uploads/") + 9);
                        java.io.File logoFile = new java.io.File("uploads", filename);
                        if (logoFile.exists()) {
                            logoImage = Image.getInstance(logoFile.getAbsolutePath());
                        }
                    } else if (new java.io.File(logoUrl).exists()) {
                        logoImage = Image.getInstance(logoUrl);
                    }
                    if (logoImage != null) {
                        logoImage.scaleToFit(50, 35);
                    }
                } catch (Exception ignored) {}
            }
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(203, 213, 225));
            Font headerCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_SLATE);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
            Font smallMutedFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);
            Font netPayLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_NAVY);
            Font netPayAmountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PRIMARY_NAVY);

            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

            // 1. Header Banner (Using Dynamic Company Profile)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{62f, 38f});

            PdfPCell leftHeader = new PdfPCell();
            leftHeader.setBackgroundColor(PRIMARY_NAVY);
            leftHeader.setPadding(10f);
            leftHeader.setBorder(PdfPCell.NO_BORDER);

            if (logoImage != null) {
                PdfPTable logoBrandTable = new PdfPTable(2);
                logoBrandTable.setWidthPercentage(100);
                logoBrandTable.setWidths(new float[]{22f, 78f});

                PdfPCell logoCell = new PdfPCell(logoImage, false);
                logoCell.setBorder(PdfPCell.NO_BORDER);
                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                logoBrandTable.addCell(logoCell);

                PdfPCell textCell = new PdfPCell();
                textCell.setBorder(PdfPCell.NO_BORDER);
                textCell.addElement(new Paragraph(companyName, titleFont));
                textCell.addElement(new Paragraph(companyAddress, subTitleFont));
                logoBrandTable.addCell(textCell);

                leftHeader.addElement(logoBrandTable);
            } else {
                leftHeader.addElement(new Paragraph(companyName, titleFont));
                leftHeader.addElement(new Paragraph(companyAddress, subTitleFont));
            }
            headerTable.addCell(leftHeader);

            PdfPCell rightHeader = new PdfPCell();
            rightHeader.setBackgroundColor(PRIMARY_NAVY);
            rightHeader.setPadding(10f);
            rightHeader.setBorder(PdfPCell.NO_BORDER);
            rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph pRightTitle = new Paragraph("PAYSLIP STATEMENT", titleFont);
            pRightTitle.setAlignment(Element.ALIGN_RIGHT);
            rightHeader.addElement(pRightTitle);

            String periodName = item.getPayrollRun() != null && item.getPayrollRun().getPeriodName() != null
                    ? item.getPayrollRun().getPeriodName()
                    : "Period: N/A";
            Paragraph pRightPeriod = new Paragraph(periodName, subTitleFont);
            pRightPeriod.setAlignment(Element.ALIGN_RIGHT);
            rightHeader.addElement(pRightPeriod);

            headerTable.addCell(rightHeader);
            document.add(headerTable);

            document.add(new Paragraph(" ", smallMutedFont));

            // 2. Employee Details Box
            PdfPTable empTable = new PdfPTable(4);
            empTable.setWidthPercentage(100);
            empTable.setWidths(new float[]{18f, 32f, 18f, 32f});

            String empName = item.getEmployee() != null && item.getEmployee().getFullName() != null
                    ? item.getEmployee().getFullName().getFullName()
                    : "Employee";
            String empIdStr = item.getEmployee() != null && item.getEmployee().getEmployeeId() != null
                    ? item.getEmployee().getEmployeeId()
                    : (item.getEmployee() != null ? "EMP-" + item.getEmployee().getId() : "N/A");
            String posTitle = item.getEmployee() != null && item.getEmployee().getPosition() != null
                    ? item.getEmployee().getPosition().getTitle()
                    : "N/A";
            String deptName = item.getEmployee() != null && item.getEmployee().getPosition() != null && item.getEmployee().getPosition().getDepartment() != null
                    ? item.getEmployee().getPosition().getDepartment().getName()
                    : "N/A";

            addEmpCell(empTable, "Employee Name:", boldFont, empName, regularFont);
            addEmpCell(empTable, "Employee ID:", boldFont, empIdStr, regularFont);
            addEmpCell(empTable, "Department:", boldFont, deptName, regularFont);
            addEmpCell(empTable, "Position:", boldFont, posTitle, regularFont);

            String statusStr = item.getPayrollRun() != null && item.getPayrollRun().getStatus() != null
                    ? item.getPayrollRun().getStatus().name()
                    : "FINALIZED";
            String runPeriod = item.getPayrollRun() != null && item.getPayrollRun().getPeriodStart() != null && item.getPayrollRun().getPeriodEnd() != null
                    ? item.getPayrollRun().getPeriodStart().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " - " + item.getPayrollRun().getPeriodEnd().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : "N/A";

            addEmpCell(empTable, "Pay Period:", boldFont, runPeriod, regularFont);
            addEmpCell(empTable, "Status:", boldFont, statusStr, regularFont);

            document.add(empTable);

            document.add(new Paragraph(" ", smallMutedFont));

            // 3. Earnings & Deductions Table
            PdfPTable sideBySide = new PdfPTable(2);
            sideBySide.setWidthPercentage(100);
            sideBySide.setWidths(new float[]{50f, 50f});

            // Earnings Column (Table)
            PdfPTable earnTable = new PdfPTable(2);
            earnTable.setWidthPercentage(100);
            earnTable.setWidths(new float[]{65f, 35f});

            addTableHeader(earnTable, "EARNINGS (PENDAPATAN)", headerCellFont);
            addBreakdownRow(earnTable, "Base Salary", fmt(item.getBaseSalary(), format), regularFont);
            addBreakdownRow(earnTable, "Overtime Pay", fmt(item.getOvertimePay(), format), regularFont);
            addBreakdownRow(earnTable, "Allowances", fmt(item.getAllowances(), format), regularFont);
            addTotalRow(earnTable, "TOTAL GROSS PAY", fmt(item.getGrossPay(), format), boldFont);

            // Deductions Column (Table)
            PdfPTable dedTable = new PdfPTable(2);
            dedTable.setWidthPercentage(100);
            dedTable.setWidths(new float[]{65f, 35f});

            addTableHeader(dedTable, "DEDUCTIONS (POTONGAN)", headerCellFont);
            addBreakdownRow(dedTable, "Late Deductions", fmt(item.getLateDeductions(), format), regularFont);
            addBreakdownRow(dedTable, "Absence Deductions", fmt(item.getAbsenceDeductions(), format), regularFont);
            addBreakdownRow(dedTable, "BPJS Kesehatan (1%)", fmt(item.getBpjsKesehatanEmployee(), format), regularFont);
            addBreakdownRow(dedTable, "BPJS Ketenagakerjaan (2%)", fmt(item.getBpjsKetenagakerjaanEmployee(), format), regularFont);
            addBreakdownRow(dedTable, "PPh 21 Tax", fmt(item.getPph21Tax(), format), regularFont);
            addTotalRow(dedTable, "TOTAL DEDUCTIONS", fmt(item.getTotalDeductions(), format), boldFont);

            PdfPCell leftCell = new PdfPCell(earnTable);
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.setPaddingRight(4f);
            sideBySide.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell(dedTable);
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.setPaddingLeft(4f);
            sideBySide.addCell(rightCell);

            document.add(sideBySide);

            document.add(new Paragraph(" ", smallMutedFont));

            // 4. Company BPJS Contribution Box
            PdfPTable companyBpjsTable = new PdfPTable(2);
            companyBpjsTable.setWidthPercentage(100);
            companyBpjsTable.setWidths(new float[]{70f, 30f});

            addTableHeader(companyBpjsTable, "EMPLOYER BPJS CONTRIBUTIONS (TANGGUNGAN PERUSAHAAN)", headerCellFont);
            addBreakdownRow(companyBpjsTable, "BPJS Kesehatan Employer Share (4%)", fmt(item.getBpjsKesehatanCompany(), format), regularFont);
            addBreakdownRow(companyBpjsTable, "BPJS Ketenagakerjaan Employer Share (4.24%)", fmt(item.getBpjsKetenagakerjaanCompany(), format), regularFont);

            document.add(companyBpjsTable);

            document.add(new Paragraph(" ", smallMutedFont));

            // 5. Net Pay Take-Home Box
            PdfPTable netPayTable = new PdfPTable(2);
            netPayTable.setWidthPercentage(100);
            netPayTable.setWidths(new float[]{50f, 50f});

            PdfPCell netLabelCell = new PdfPCell(new Phrase("NET TAKE-HOME PAY / GAJI BERSIH DITERIMA", netPayLabelFont));
            netLabelCell.setBackgroundColor(ACCENT_BLUE);
            netLabelCell.setBorderColor(BLUE_BORDER);
            netLabelCell.setPadding(10f);
            netLabelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            netPayTable.addCell(netLabelCell);

            PdfPCell netValCell = new PdfPCell(new Phrase(fmt(item.getNetPay(), format), netPayAmountFont));
            netValCell.setBackgroundColor(ACCENT_BLUE);
            netValCell.setBorderColor(BLUE_BORDER);
            netValCell.setPadding(10f);
            netValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            netValCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            netPayTable.addCell(netValCell);

            document.add(netPayTable);

            document.add(new Paragraph(" ", smallMutedFont));
            document.add(new Paragraph(" ", smallMutedFont));

            // 6. Signature Block (Standard Left-Right Formal Signing Template)
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setWidths(new float[]{50f, 50f});

            String docHash = generateDocHash(item);

            // Determine HR / Authorized signer name
            String hrSignerName = item.getPayrollRun() != null && item.getPayrollRun().getCreatedBy() != null && item.getPayrollRun().getCreatedBy().getFullName() != null
                    ? item.getPayrollRun().getCreatedBy().getFullName().getFullName()
                    : "HR & Finance Department";

            // Left Side: Employee Signature
            PdfPCell sigLeft = new PdfPCell();
            sigLeft.setBorder(PdfPCell.NO_BORDER);
            sigLeft.setHorizontalAlignment(Element.ALIGN_LEFT);

            String empDateStr = Boolean.TRUE.equals(item.getIsAcknowledged()) && item.getAcknowledgedAt() != null
                    ? "Date: " + item.getAcknowledgedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                    : "Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            Paragraph pEmpDate = new Paragraph(empDateStr, regularFont);
            sigLeft.addElement(pEmpDate);

            Paragraph pEmpTitle = new Paragraph("Employee Signature / Tanda Tangan Karyawan:", regularFont);
            sigLeft.addElement(pEmpTitle);

            if (Boolean.TRUE.equals(item.getIsAcknowledged())) {
                String empAckPayload = String.format(
                        "=== EMPLOYEE DIGITAL ACKNOWLEDGMENT ===\n" +
                        "Employee: %s (%s)\n" +
                        "Status: ACKNOWLEDGED & SIGNED\n" +
                        "Date: %s\n" +
                        "IP Address: %s\n" +
                        "Doc Hash Ref: %s\n" +
                        "======================================",
                        empName,
                        empIdStr,
                        item.getAcknowledgedAt() != null ? item.getAcknowledgedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")) : "N/A",
                        item.getAcknowledgmentIp() != null ? item.getAcknowledgmentIp() : "Verified",
                        docHash
                );
                Image empQr = createQrCodeImage(empAckPayload, 55);
                if (empQr != null) {
                    empQr.setAlignment(Image.ALIGN_LEFT);
                    sigLeft.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 3)));
                    sigLeft.addElement(empQr);
                    sigLeft.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 3)));
                }

                Paragraph pEmpName = new Paragraph(empName, boldFont);
                sigLeft.addElement(pEmpName);
                
                Paragraph pEmpSub = new Paragraph("[ ELECTRONICALLY SIGNED ]", smallMutedFont);
                sigLeft.addElement(pEmpSub);
            } else {
                sigLeft.addElement(new Paragraph("\n\n\n___________________________", boldFont));
                Paragraph pEmpName = new Paragraph(empName, boldFont);
                sigLeft.addElement(pEmpName);
            }
            sigTable.addCell(sigLeft);

            // Right Side: Authorized HR / Finance Officer
            PdfPCell sigRight = new PdfPCell();
            sigRight.setBorder(PdfPCell.NO_BORDER);
            sigRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

            String issueDateStr = "Date: " + (item.getPayrollRun() != null && item.getPayrollRun().getCreatedAt() != null
                    ? item.getPayrollRun().getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            Paragraph pHrDate = new Paragraph(issueDateStr, regularFont);
            pHrDate.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(pHrDate);

            Paragraph pHrTitle = new Paragraph("Authorized HR / Finance Officer:", regularFont);
            pHrTitle.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(pHrTitle);

            String corpPayload = String.format(
                    "=== %s ===\n" +
                    "OFFICIAL PAYSLIP VERIFICATION SEAL\n" +
                    "Doc ID: PAYSLIP-%d\n" +
                    "Employee: %s (%s)\n" +
                    "Period: %s\n" +
                    "Net Pay: %s\n" +
                    "Status: FINALIZED\n" +
                    "Doc Hash: %s\n" +
                    "Issued: %s\n" +
                    "Issuer: HR & Payroll Department\n" +
                    "===========================",
                    companyName,
                    item.getId() != null ? item.getId() : 0,
                    empName,
                    empIdStr,
                    periodName,
                    fmt(item.getNetPay(), format),
                    docHash,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            );
            Image corpQr = createQrCodeImage(corpPayload, 55);
            if (corpQr != null) {
                corpQr.setAlignment(Image.ALIGN_RIGHT);
                sigRight.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 3)));
                sigRight.addElement(corpQr);
                sigRight.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 3)));
            }

            Paragraph pHrName = new Paragraph(hrSignerName, boldFont);
            pHrName.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(pHrName);

            Paragraph pHrSub = new Paragraph(companyName + "\nHR & Payroll Department", smallMutedFont);
            pHrSub.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(pHrSub);

            sigTable.addCell(sigRight);

            document.add(sigTable);

            document.add(new Paragraph(" ", smallMutedFont));

            // 7. Confidentiality Footer
            Paragraph footer = new Paragraph(
                    "Note: This payslip is computer-generated and serves as an official statement of salary and tax deductions. Please keep this confidential.",
                    smallMutedFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }

    private void addEmpCell(PdfPTable table, String label, Font labelFont, String value, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBackgroundColor(BG_LIGHT);
        c1.setBorderColor(BORDER_COLOR);
        c1.setPadding(5f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", valFont));
        c2.setBackgroundColor(Color.WHITE);
        c2.setBorderColor(BORDER_COLOR);
        c2.setPadding(5f);
        table.addCell(c2);
    }

    private void addTableHeader(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setColspan(table.getNumberOfColumns());
        cell.setBackgroundColor(BG_HEADER);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addBreakdownRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorderColor(BORDER_COLOR);
        c1.setPadding(5f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorderColor(BORDER_COLOR);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(5f);
        table.addCell(c2);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBackgroundColor(BG_HEADER);
        c1.setBorderColor(BORDER_COLOR);
        c1.setPadding(6f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBackgroundColor(BG_HEADER);
        c2.setBorderColor(BORDER_COLOR);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(6f);
        table.addCell(c2);
    }

    private String fmt(BigDecimal amount, NumberFormat format) {
        if (amount == null) {
            return format.format(BigDecimal.ZERO);
        }
        return format.format(amount);
    }
    
    private String generateDocHash(PayrollItem item) {
        try {
            String raw = "PAYROLL-" + item.getId() + "-EMP-" + (item.getEmployee() != null ? item.getEmployee().getId() : "0") + "-" + item.getNetPay();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().substring(0, 16).toUpperCase();
        }
    }

    private Image createQrCodeImage(String content, int size) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 180, 180);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            Image image = Image.getInstance(pngData);
            image.scaleAbsolute((float) size, (float) size);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

