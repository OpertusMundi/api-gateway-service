package eu.opertusmundi.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.FileUploadEntity;

@Repository
@Transactional
public interface FileUploadRepository extends JpaRepository<FileUploadEntity, Integer> {

}
