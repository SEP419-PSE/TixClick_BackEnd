package com.pse.tixclick.service.impl;

import com.aspose.pdf.DocMDPAccessPermissions;
import com.aspose.pdf.DocMDPSignature;
import com.aspose.pdf.PKCS7;
import com.aspose.pdf.facades.PdfFileSignature;
import com.cloudinary.utils.ObjectUtils;
import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import com.pse.tixclick.payload.entity.company.ContractDocument;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.ContractDocumentRepository;
import com.pse.tixclick.repository.ContractRepository;
import com.pse.tixclick.service.ContractDocumentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractDocumentServiceImpl implements ContractDocumentService {
    ContractDocumentRepository contractDocumentRepository;
    AccountRepository accountRepository;
    CloudinaryService cloudinaryService;
    ContractRepository contractRepository;
    ModelMapper modelMapper;

    @Override
    public ContractDocumentDTO uploadContractDocument(MultipartFile file, int contractId) throws IOException {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String fileURL = cloudinaryService.uploadDocumentToCloudinary(file);

        var contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        var contractDocument = new ContractDocument();
        contractDocument.setContract(contract);
        contractDocument.setFileName(file.getOriginalFilename());
        contractDocument.setFileURL(fileURL);
        contractDocument.setFileType(file.getContentType());
        contractDocument.setUploadedBy(account);
        contractDocument.setUploadDate(java.time.LocalDateTime.now());
        contractDocumentRepository.save(contractDocument);

        return modelMapper.map(contractDocument, ContractDocumentDTO.class);
    }

    @Override
    public ContractDocumentDTO getContractDocument(int contractId) {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_DOCUMENT_NOT_FOUND));
        return modelMapper.map(contractDocument, ContractDocumentDTO.class);
    }

    @Override
    public List<ContractDocumentDTO> getContractDocumentsByContract(int contractId) {
        List<ContractDocument> contractDocuments = contractDocumentRepository.findByAllByContractId(contractId);
        return contractDocuments.stream()
                .map(contractDocument -> modelMapper.map(contractDocument, ContractDocumentDTO.class))
                .toList();
    }

    @Override
    public void deleteContractDocument(int contractDocumentId) throws IOException {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractDocumentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_DOCUMENT_NOT_FOUND));
        contractDocumentRepository.delete(contractDocument);
        String publicId = cloudinaryService.extractPublicId(contractDocument.getFileURL());

        Map result = cloudinaryService.deleteFile(publicId);
        if (result.get("result").equals("not found")) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public List<ContractDocumentDTO> getAllContractDocuments() {
        List<ContractDocument> contractDocuments = contractDocumentRepository.findAll();
        return contractDocuments.stream()
                .map(contractDocument -> modelMapper.map(contractDocument, ContractDocumentDTO.class))
                .toList();
    }

    @Override
    public List<ContractDocumentDTO> getContractDocumentsByEvent(int eventId) {
        List<ContractDocument> contractDocuments = contractDocumentRepository.findByAllByEventId(eventId);
        return contractDocuments.stream()
                .map(contractDocument -> modelMapper.map(contractDocument, ContractDocumentDTO.class))
                .toList();
    }

    @Override
    public List<ContractDocumentDTO> getContractDocumentsByCompany(int companyId) {
        List<ContractDocument> contractDocuments = contractDocumentRepository.findByAllByCompanyId(companyId);
        return contractDocuments.stream()
                .map(contractDocument -> modelMapper.map(contractDocument, ContractDocumentDTO.class))
                .toList();
    }

    @Override
    public File signPdf(String pdfUrl, String name) throws Exception {
        return null;
    }

//    @Override
//    public File signPdf(String pdfUrl, String name) throws Exception {
//        // Tải file PDF từ Cloudinary
//        File pdfFile = downloadFromCloudinary(pdfUrl);
//
//        // Tạo chữ ký
//        File signatureImage = generateSignatureImage(name);
//
//        // Mở file PDF
//        Document doc = new Document(pdfFile.getAbsolutePath());
//        PdfFileSignature signature = new PdfFileSignature(doc);
//
//        // Load chứng chỉ (Nếu cần)
//        PKCS7 pkcs = new PKCS7("certificate.pfx", "password");
//        DocMDPSignature docMdpSignature = new DocMDPSignature(pkcs, DocMDPAccessPermissions.FillingInForms);
//
//        // Vị trí chèn chữ ký (trang 1)
//        java.awt.Rectangle rect = new java.awt.Rectangle(150, 650, 450, 750);
//
//        // Gán ảnh chữ ký
//        signature.setSignatureAppearance(signatureImage.getAbsolutePath());
//
//        // Ký file
//        signature.certify(1, "Ký tài liệu", "contact@example.com", "Vietnam", true, rect, docMdpSignature);
//
//        // Lưu file đã ký
//        File signedPdf = File.createTempFile("signed_", ".pdf");
//        signature.save(signedPdf.getAbsolutePath());
//
//        return signedPdf;
//    }

    private File generateSignatureImage(String name) throws IOException {
        int width = 400, height = 100;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Cấu hình font chữ
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString(name, 50, 60);
        g2d.dispose();

        // Lưu file chữ ký
        File signatureFile = File.createTempFile("signature_", ".png");
        ImageIO.write(image, "png", signatureFile);
        return signatureFile;
    }

    private File downloadFromCloudinary(String fileUrl) throws IOException {
        java.net.URL url = new java.net.URL(fileUrl);
        java.io.InputStream in = url.openStream();
        File tempFile = File.createTempFile("downloaded_", ".pdf");
        java.nio.file.Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

//    public String uploadToCloudinary(File file) throws Exception {
//        Map uploadResult = cloudinaryService.uploadDocumentToCloudinary(file, ObjectUtils.asMap("resource_type", "auto"));
//        return uploadResult.get("secure_url").toString();
//    }
}
