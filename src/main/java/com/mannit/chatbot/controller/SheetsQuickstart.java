package com.mannit.chatbot.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.mannit.chatbot.model.CurrentPatient;
import com.mannit.chatbot.model.Noappointment;
import com.mannit.chatbot.model.QueriedPatient;
import com.mannit.chatbot.repository.Currentpatientsrepo;
import com.mannit.chatbot.repository.Noappointmentrepo;
import com.mannit.chatbot.repository.QuriedpRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@RestController
public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private final static Logger logger = LoggerFactory.getLogger(SheetsQuickstart.class);
    @Autowired
    private Currentpatientsrepo repo;
    @Autowired
    private Noappointmentrepo no_app_repo;
    @Autowired
    private QuriedpRepo quried_repo;

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private String lastProcessedTimestampYesPatients = "";
    private String lastProcessedTimestampNoAppointment = "";
    private String lastProcessedTimestampCallBack = "";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    	 logger.info("<in the getcredentials method>");
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Scheduled(fixedRate = 10000)
    public void getsheetdata() throws GeneralSecurityException, IOException {
    	logger.info("<In the getSheetdata() method>");
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "16mQkUXa6PeeH96WDFeATHPLFx_dYUUOGcXqJ9clkBek";
        final String range_1 = "Yes-patients";
        final String range_2 = "No-appointment";
        final String range_3 = "Call-me-back";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        logger.info("<Started Reading the spreadsheet with spreadsheet id >"+ spreadsheetId);
        ValueRange response_1 = service.spreadsheets().values().get(spreadsheetId, range_1).execute();
        List<List<Object>> values1 = response_1.getValues();
        lastProcessedTimestampYesPatients = processSheetData("Yes-patients", values1, lastProcessedTimestampYesPatients);

        ValueRange response_2 = service.spreadsheets().values().get(spreadsheetId, range_2).execute();
        List<List<Object>> values2 = response_2.getValues();
        lastProcessedTimestampNoAppointment = processSheetData("No-appointment", values2, lastProcessedTimestampNoAppointment);

        ValueRange response_3 = service.spreadsheets().values().get(spreadsheetId, range_3).execute();
        List<List<Object>> values3 = response_3.getValues();
        lastProcessedTimestampCallBack = processSheetData("Call-me-back", values3, lastProcessedTimestampCallBack);
    }

    private String processSheetData(String sheetName, List<List<Object>> values, String lastProcessedTimestamp) {
        boolean isFirstRow = true;

        if (values == null || values.isEmpty()) {
            System.out.println("No data found for " + sheetName);
        } else {
            for (List<Object> row : values) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                String timestamp = row.get(0).toString();

                if (timestamp.compareTo(lastProcessedTimestamp) > 0) {
                    if (sheetName.equals("Yes-patients")) {
                        CurrentPatient cp = new CurrentPatient();
                        cp.setTimestamp(row.get(0).toString());
                        cp.setName(row.get(1).toString());
                        cp.setPhone_number(row.get(2).toString());
                        cp.setDoctor_choice(row.get(3).toString());
                        repo.save(cp);
                    } else if (sheetName.equals("No-appointment")) {
                        Noappointment noAppointment = new Noappointment();
                        noAppointment.setName(row.get(1).toString());
                        noAppointment.setPhone_number(row.get(2).toString());
                        noAppointment.setTimestamp(row.get(0).toString());
                        no_app_repo.save(noAppointment);
                    } else if (sheetName.equals("Call-me-back")) {
                        QueriedPatient qp = new QueriedPatient();
                        qp.setName(row.get(1).toString());
                        qp.setPhone_number(row.get(2).toString());
                        qp.setTimestamp(row.get(0).toString());
                        quried_repo.save(qp);
                    }

                    lastProcessedTimestamp = timestamp;
                }
            }
        }

        return lastProcessedTimestamp;
    }
}
