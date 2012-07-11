/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.sender;

import static org.acra.ACRA.LOG_TAG;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.CrashReportData;
import org.acra.ReportField;
import org.acra.util.HttpUtils;

import android.net.Uri;
import android.util.Log;

/**
 * ACRA's default {@link ReportSender}: sends report data to a GoogleDocs Form.
 * 
 * @author Kevin Gaudin
 * 
 */
public class GoogleFormSender implements ReportSender {
    private Uri mFormUri = null;

    /**
     * Creates a new GoogleFormSender which will send data to a Form identified by its key.
     * @param formKey The key of the form. The key is the formKey parameter value in the Form Url: https://spreadsheets.google.com/viewform?formkey=<b>dDN6NDdnN2I2aWU1SW5XNmNyWVljWmc6MQ</b>
     */
    public GoogleFormSender(String formKey) {
        mFormUri = Uri.parse("https://spreadsheets.google.com/formResponse?formkey=" + formKey + "&amp;ifq");
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        Map<String, String> formParams = remap(report);
        // values observed in the GoogleDocs original html form
        formParams.put("pageNumber", "0");
        formParams.put("backupCache", "");
        formParams.put("submit", "Envoyer");

        try {
            final URL reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Sending report " + report.get(ReportField.REPORT_ID));
            Log.d(LOG_TAG, "Connect to " + reportUrl);
            HttpUtils.doPost(formParams, reportUrl, null, null);
        } catch (IOException e) {
            throw new ReportSenderException("Error while sending report to Google Form.", e);
        }

    }

    private Map<String, String> remap(Map<ReportField, String> report) {
        Map<String, String> result = new HashMap<String, String>();

        int inputId = 0;
        ReportField[] fields = ACRA.getConfig().customReportContent();
        if(fields.length == 0) {
            fields = ACRA.DEFAULT_REPORT_FIELDS;
        }
        for (Object originalKey : fields) {
            switch ((ReportField) originalKey) {
            case APP_VERSION_NAME:
                result.put("entry." + inputId + ".single", "'" + report.get(originalKey));
                break;
            case ANDROID_VERSION:
                result.put("entry." + inputId + ".single", "'" + report.get(originalKey));
                break;
            default:
                result.put("entry." + inputId + ".single", report.get(originalKey));
                break;
            }
            inputId++;
        }
        return result;
    }

}
