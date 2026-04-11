package com.alethia.AuthentiFace.MailService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alethia.AuthentiFace.MailService.Entity.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByMailId(UUID mailId);
}
