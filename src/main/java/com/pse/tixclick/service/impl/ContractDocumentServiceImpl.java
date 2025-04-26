package com.pse.tixclick.service.impl;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.ContractDocument;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.ContractDocumentRepository;
import com.pse.tixclick.repository.ContractRepository;
import com.pse.tixclick.service.ContractDocumentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.security.auth.x500.X500Principal;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractDocumentServiceImpl implements ContractDocumentService {
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }    }
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
        contractDocument.setStatus("PENDING");
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
        List<ContractDocumentDTO> contractDocumentDTOs = new ArrayList<>();

        for (ContractDocument contractDocument : contractDocuments) {
            ContractDocumentDTO dto = new ContractDocumentDTO();
            dto.setContractDocumentId(contractDocument.getContractDocumentId());
            dto.setContractId(contractDocument.getContract().getContractId()); // nếu có entity Contract
            dto.setFileName(contractDocument.getFileName());
            dto.setFileURL(contractDocument.getFileURL());
            dto.setFileType(contractDocument.getFileType());
            dto.setUploadedBy(contractDocument.getUploadedBy().getAccountId());
            dto.setStatus(contractDocument.getStatus());
            dto.setUploadDate(contractDocument.getUploadDate());
            dto.setContractCode(contractDocument.getContract().getContractCode()); // nếu Contract có field contractCode

            contractDocumentDTOs.add(dto);
        }
        return contractDocumentDTOs;
    }


    @Override
    public List<ContractDocumentDTO> getContractDocumentsByCompany(int companyId) {
        List<ContractDocument> contractDocuments = contractDocumentRepository.findByAllByCompanyId(companyId);
        return contractDocuments.stream()
                .map(contractDocument -> modelMapper.map(contractDocument, ContractDocumentDTO.class))
                .toList();
    }

    @Override
    public String signPdf(int contractDocumentId) throws Exception {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractDocumentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_DOCUMENT_NOT_FOUND));

        Company company = contractDocument.getContract().getCompany();

        File tempPdf = downloadFromCloudinary(contractDocument.getFileURL());
        if (tempPdf == null) {
            throw new RuntimeException("Failed to download PDF file.");
        }

        File signedPdf = new File("signed_contract.pdf");
        PrivateKey privateKey = generateKeyPair().getPrivate();

        try (PdfReader reader = new PdfReader(tempPdf.getAbsolutePath());
             FileOutputStream os = new FileOutputStream(signedPdf)) {

            PdfSigner signer = new PdfSigner(reader, os, new StampingProperties());

            // Tạo chữ ký số bằng RSA
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            try (InputStream inputStream = new FileInputStream(tempPdf)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    signature.update(buffer, 0, bytesRead);
                }
            }

            byte[] signedBytes = signature.sign();
            String encodedSignature = Base64.getEncoder().encodeToString(signedBytes);

            // Thêm chữ ký vào file PDF
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setLocation(company.getAddress());
            appearance.setReason("Contract authentication");
            appearance.setPageNumber(1);
            appearance.setPageRect(new com.itextpdf.kernel.geom.Rectangle(300, 100, 200, 50));

            IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC");
            IExternalDigest digest = new BouncyCastleDigest();

            KeyPair keyPair = generateKeyPair();
            X509Certificate cert = generateSelfSignedCertificate(keyPair, company.getCompanyName());
            X509Certificate[] chain = new X509Certificate[]{cert}; ; // Chuyển thành mảng đúng định dạng

            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CADES);
        }

        // 3. Upload file đã ký lên Cloudinary
        String cloudUrl = cloudinaryService.uploadFile(signedPdf);
        contractDocument.setFileURL(cloudUrl);
        contractDocumentRepository.save(contractDocument);

        // 4. Xóa file local
        deleteFile(tempPdf);
        deleteFile(signedPdf);

        return cloudUrl;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }



    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String companyName) throws Exception {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date expiryDate = new Date(now + (365L * 24 * 60 * 60 * 1000)); // Hết hạn sau 1 năm

        X500Principal subject = new X500Principal(companyName);

        // Sử dụng số serial ngẫu nhiên để tránh lỗi trùng
        BigInteger serial = new BigInteger(64, new java.security.SecureRandom());

        // Đảm bảo thuật toán SHA256withRSA không bị null
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        signerBuilder.setProvider("BC");
        ContentSigner signer = signerBuilder.build(keyPair.getPrivate());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, startDate, expiryDate, subject, keyPair.getPublic());

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certBuilder.build(signer));
    }

    public static X509Certificate generateCertificate(PrivateKey privateKey) throws Exception {
        KeyPair keyPair = new KeyPair(generateKeyPair().getPublic(), privateKey);
        return generateSelfSignedCertificate(keyPair, "TixClick");
    }

    public static String signData(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signedBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signedBytes);
    }

    private void deleteFile(File file) {
        if (file != null && file.exists() && file.delete()) {
            System.out.println("Deleted file: " + file.getAbsolutePath());
        } else {
            System.out.println("Failed to delete file: " + file.getAbsolutePath());
        }
    }

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
}
