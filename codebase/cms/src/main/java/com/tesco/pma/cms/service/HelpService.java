package com.tesco.pma.cms.service;

import com.tesco.pma.colleague.api.Colleague;

import java.util.Collection;
import java.util.Map;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-15 23:19
 */
public interface HelpService {
    Map<String, String> getHelpFaqUrls(Colleague colleague, Collection<String> keys);
}
