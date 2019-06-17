package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.ContactType;
import com.rbkmoney.cashreg.repository.ContactTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class ContactTypeService {

    private ContactTypeRepository contactTypeRepository;


    @Autowired
    public ContactTypeService(ContactTypeRepository contactTypeRepository) {
        Assert.notNull(contactTypeRepository, "ContactTypeRepository must not be null");

        this.contactTypeRepository = contactTypeRepository;
    }

    public ContactType findByContactType(String contactType) {
        log.debug("Trying to find ContactType by contactType {} from the DB", contactType);
        ContactType response = contactTypeRepository.findByType(contactType);
        log.debug("Finish ContactType method. DB returned [{}]", response);
        return response;
    }

    public ContactType save(ContactType inputContactType) {
        log.debug("Trying to save ContactType from the DB");
        ContactType response = contactTypeRepository.save(inputContactType);
        log.debug("Finish ContactType save method. DB returned [{}]", response);
        return response;
    }

}
