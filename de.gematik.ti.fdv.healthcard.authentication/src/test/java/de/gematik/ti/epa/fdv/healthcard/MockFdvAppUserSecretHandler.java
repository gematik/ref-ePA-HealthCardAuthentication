/*
 * Copyright (c) 2020 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.ti.epa.fdv.healthcard;

import org.greenrobot.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.openhealthcard.events.message.AbstractOpenHealthCardEvent;
import de.gematik.ti.openhealthcard.events.message.ErrorEvent;
import de.gematik.ti.openhealthcard.events.request.RequestCardAccessNumberEvent;
import de.gematik.ti.openhealthcard.events.request.RequestPinNumberEvent;
import de.gematik.ti.openhealthcard.events.response.entities.CardAccessNumber;
import de.gematik.ti.openhealthcard.events.response.entities.PinNumber;

/**
 * A Mock of user interface of App 
 * 
 */
public class MockFdvAppUserSecretHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MockFdvAppUserSecretHandler.class);

    private String[] getPinStatusRequestResponse;
    private String[] verifyRequestResponse;
    private String pinInput = "123452";
    private final String cardAccessNumber = "12345678";
    private String errorText;

    /**
     * constructor
     */
    public MockFdvAppUserSecretHandler() {
        LOG.debug("CanPinRequestHandlerMock logged");
    }

    /**
     * constructor with parameter
     * @param getPinStatusRequestResponse
     * @param verifyRequestResponse
     */
    public MockFdvAppUserSecretHandler(final String[] getPinStatusRequestResponse, final String[] verifyRequestResponse) {
        this.getPinStatusRequestResponse = getPinStatusRequestResponse;
        this.verifyRequestResponse = verifyRequestResponse;
    }

    /**
     * handle RequestCardAccessNumberEvent
     * @param event
     */
    @Subscribe
    public void handleCan(final RequestCardAccessNumberEvent event) {
        final CardAccessNumber can = new CardAccessNumber(cardAccessNumber);

        LOG.debug("!can: " + can);
        event.getResponseListener().handleCan(can);
    }

    /**
     * handle RequestPinNumberEvent
     * @param event
     */
    @Subscribe
    public void handlePinNumber(final RequestPinNumberEvent event) {
        LOG.debug("event " + event);
        // get input from user
        final String pin = getPinInput();
        final String pinType = event.getPinType();
        LOG.debug("pinType: " + pinType);
        // event.getResponseListener().handlePinNumber(new PinNumber(pinType, pin));

        event.getResponseListener().handlePinNumber(new PinNumber(pinType, pin));
    }

    /**
     * request RequestCardAccessNumberEvent
     * @param event
     */
    @Subscribe
    public void requestCan(final RequestCardAccessNumberEvent event) {
        LOG.debug("!received  can event " + event);
    }

    private String getPinInput() {
        return pinInput;
    }

    /**
     * set new PIN
     * @param newPin
     */
    public void setPinInput(final String newPin) {
        pinInput = newPin;
    }

    /**
     * handle HealthCardEvent
     * @param event
     */
    @Subscribe
    public void handleMessagesFromEventBus(final AbstractOpenHealthCardEvent event) {
        if (event instanceof ErrorEvent) {
            LOG.error(event.getSourceClass() + ": " + event.getMessage());
            errorText = event.getMessage();
            ((ErrorEvent) event).getThrowable().printStackTrace();
        } else {
            LOG.info(event.getSourceClass() + ": " + event.getMessage());
        }
    }

    /**
     * return error text
     * @return
     */
    public String getErrorText() {
        return errorText;
    }

}
