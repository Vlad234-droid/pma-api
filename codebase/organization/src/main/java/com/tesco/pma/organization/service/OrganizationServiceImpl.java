package com.tesco.pma.organization.service;

import com.tesco.pma.organization.dao.OrganizationDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationDAO dao;

}
