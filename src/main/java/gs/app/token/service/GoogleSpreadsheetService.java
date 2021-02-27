package gs.app.token.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSpreadsheetService {

    private static final String LOG_PREFIX = "Google Sheet Service:";
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleSpreadsheetService.class);
    private static final String APPLICATION_NAME = "Token-GS-APP";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private String credentialsFilePath;

    public GoogleSpreadsheetService(
        @Value("${google_doc_credentials_file_path}") String credentialsFilePath) {
        this.credentialsFilePath = credentialsFilePath;
    }

    public List<List<Object>> getSpreadSheetData(final String spreadsheetId,
        final String range) throws GeneralSecurityException, IOException {

        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Sheets service = new Sheets.Builder(netHttpTransport, JSON_FACTORY,
            getCredentials(Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)))
            .setApplicationName(APPLICATION_NAME).build();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            LOGGER.info("{} No record found in spreadsheet {}", LOG_PREFIX, spreadsheetId);
            return Collections.emptyList();
        }
        else {
            LOGGER.info("{} {} records found in spreadsheet {}", LOG_PREFIX, values.size(), spreadsheetId);
            return values;
        }
    }

    /**
     *
     * @param spreadsheetId
     * @param range
     * @param row
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void addRow(final String spreadsheetId, final String range,
        final List<? super Object> row)
        throws GeneralSecurityException, IOException {

        addRows(spreadsheetId, range, Arrays.asList(row));
    }

    public void addRows(final String spreadsheetId, final String range,
        final List<List<Object>> rows)
        throws GeneralSecurityException, IOException {

        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(netHttpTransport, JSON_FACTORY,
            getCredentials(Collections.singletonList(SheetsScopes.SPREADSHEETS)))
            .setApplicationName(APPLICATION_NAME).build();

        ValueRange requestBody = new ValueRange();
        requestBody.setValues(rows);

        Sheets.Spreadsheets.Values.Append request =
            service.spreadsheets().values().append(spreadsheetId, range, requestBody);
        request.setValueInputOption("USER_ENTERED");
        request.setInsertDataOption("INSERT_ROWS");

        AppendValuesResponse response = request.execute();

        LOGGER
            .info("{} Updated sheet successfully, sheetId={}, range={}, row={}", LOG_PREFIX, spreadsheetId,
                range, rows);
    }

    /**
     *
     * @param spreadsheetId
     * @param startRowNumber
     * @param endRowNumber
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void deleteRows(final String spreadsheetId, final Integer sheetId,
        final Integer startRowNumber, final Integer endRowNumber)
        throws GeneralSecurityException, IOException {

        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(netHttpTransport, JSON_FACTORY,
            getCredentials(Collections.singletonList(SheetsScopes.SPREADSHEETS)))
            .setApplicationName(APPLICATION_NAME).build();


        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        Request request = new Request()
            .setDeleteDimension(new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                    .setSheetId(sheetId)
                    .setDimension("ROWS")
                    .setStartIndex(startRowNumber)
                    .setEndIndex(endRowNumber)
                )
            );

        List<Request> requests = new ArrayList<Request>();
        requests.add(request);
        content.setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, content).execute();

        LOGGER
            .info("{} Deleted sheet rows successfully, sheetId={}, startRowNumber={}, endRowNumber={}", LOG_PREFIX,
                spreadsheetId,
                startRowNumber, endRowNumber);
    }


    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private HttpRequestInitializer getCredentials(final Collection<String> scopes) throws IOException {
        InputStream in = Files.newInputStream(Paths.get(credentialsFilePath));
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
        }
        return new HttpCredentialsAdapter(ServiceAccountCredentials.fromStream(in).createScoped(scopes));
    }

}
