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

package de.gematik.ti.epa.fdv.healthcard.authentication;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.epa.fdv.authentication.service.provider.api.IAuthenticationResult;
import de.gematik.ti.epa.fdv.authentication.service.provider.api.IAuthenticator;
import de.gematik.ti.epa.fdv.authentication.service.provider.api.ICertificateHolder;
import de.gematik.ti.epa.fdv.authentication.service.provider.api.entities.AuthenticationResult;
import de.gematik.ti.epa.fdv.authentication.service.provider.api.entities.AuthenticationState;
import de.gematik.ti.epa.fdv.healthcard.authentication.healthcard.EgkCertificateLoader;
import de.gematik.ti.epa.fdv.healthcard.authentication.healthcard.EgkHashSigner;
import de.gematik.ti.healthcard.control.common.verifyPin.PinResult;
import de.gematik.ti.healthcard.control.common.verifyPin.PinVerfiyLauncher;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.operation.Subscriber;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.openhealthcard.events.message.ErrorEvent;

/**
 * implements {@link IAuthenticator}.
 */
public class Authenticator implements IAuthenticator<IHealthCard> {
    private static final Logger LOG = LoggerFactory.getLogger(Authenticator.class);
    private final AuthenticationController authenticationController;

    /**
     * constructor, in which AuthenticationController starts
     */
    public Authenticator() {
        authenticationController = new AuthenticationController().start();
    }

    /**
     * sign the coming data
     * @param hashValue
     * @return
     */
    @Override
    public IAuthenticationResult signData(final byte[] hashValue) {
        return authenticate(authenticationController.getCertificateHolder(), hashValue);
    }

    /**
     * authentication of coming data with help of ICertificateHolder
     * @param certificateHolder
     * @param hashValue
     * @return
     */
    public AuthenticationResult authenticate(final ICertificateHolder<IHealthCard> certificateHolder, final byte[] hashValue) {
        final IHealthCard cardHc = certificateHolder.getAuthRenderer();

        final EgkHashSigner signer = new EgkHashSigner();
        //
        final AuthenticationResult[] authResults = { AuthenticationResult.DEFAULTRESULT };
        final ResultOperation<Response> resultOperation = new PinVerfiyLauncher(cardHc).verifyPin("MrPin.Home")
                .validate(pinResult -> validatePinResult(pinResult))
                .map(pinResult -> authResults[0].setRawSignData(hashValue))
                .flatMap(__ -> signer.signHashvalue(cardHc, hashValue))
                .validate(Response.ResponseStatus.SUCCESS::validateResult);
        resultOperation.subscribe(new Subscriber<Response>() {
            @Override
            public void onSuccess(final Response resp) {
                authResults[0].setAuthState(getAuthState(resp.getResponseStatus()));
                authResults[0].setHashValue(resp.getResponseData());
                LOG.info("AuthenticationResult: " + authResults[0]);
            }

            @Override
            public void onError(final Throwable t) throws RuntimeException {
                EventBus.getDefault().post(new ErrorEvent(Authenticator.class, t));
                LOG.error("Error " + t + ", rawSignData is " + authResults[0].getAuthState());
            }
        });
        return authResults[0];

    }

    private Result<PinResult> validatePinResult(final PinResult pr) {
        if (pr.isPinVerifiSuccess()) {
            return Result.success(pr);
        } else {
            return Result.failure(new Exception(pr.getVerifyResultText()));
        }
    }

    private AuthenticationState getAuthState(final Response.ResponseStatus responseStatus) {
        return responseStatus == Response.ResponseStatus.SUCCESS ? AuthenticationState.AUTH_SUCCEED : AuthenticationState.AUTH_FAILED;
    }

    /**
     * return name of the authenticator
     * @return
     */
    @Override
    public String getName() {
        return "Healthcard.Authenticator";
    }

    /**
     * get certificate in byte array
     * @return
     */
    @Override
    public byte[] getCertificateValue() {
        final ResultOperation<byte[]> resultOperation = new EgkCertificateLoader()
                .loadCertificate(authenticationController.getCertificateHolder().getAuthRenderer());
        final List<byte[]> list = new ArrayList<>();
        resultOperation.subscribe(new Subscriber<byte[]>() {
            @Override
            public void onSuccess(final byte[] resp) {
                list.add(resp);
            }

            @Override
            public void onError(final Throwable t) throws RuntimeException {
                EventBus.getDefault().post(new ErrorEvent(Authenticator.class, t));
                LOG.error("Error on getCertificate occured: " + t);
            }
        });
        if (list.size() == 1) {
            return list.get(0);
        } else {
            EventBus.getDefault().post(new ErrorEvent(Authenticator.class, "no certificate available, check the log"));
            throw new HealthCardAuthenticationException("no certificate available, check the log");
        }
    }

    /**
     * could be null. init in methode if called earlier.
     * @return
     */
    public AuthenticationController getController() {
        return authenticationController;
    }

}
