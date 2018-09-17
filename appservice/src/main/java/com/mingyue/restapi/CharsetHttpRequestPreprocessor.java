package com.mingyue.restapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpRequestPreprocessor;

public class CharsetHttpRequestPreprocessor implements HttpRequestPreprocessor {
        private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

        public void preProcess(HttpRequest request) {
                LOG.info("CharsetHttpRequestPreprocessor effected.");
                request.setAttribute(InputPart.DEFAULT_CHARSET_PROPERTY, "UTF-8");
                request.setAttribute(InputPart.DEFAULT_CONTENT_TYPE_PROPERTY, "*/*; charset=UTF-8");
        }
}