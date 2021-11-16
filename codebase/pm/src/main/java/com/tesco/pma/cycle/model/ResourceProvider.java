package com.tesco.pma.cycle.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 17.10.2021 Time: 22:54
 */
public interface ResourceProvider {
    InputStream read(String resourceName) throws IOException;

    String resourceToString(String resourceName) throws IOException;
}
