package org.samo_lego.fabrictailor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TailorConfig {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
}
