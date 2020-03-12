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

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.openhealthcard.events.request.RequestPaceKeyEvent;
import de.gematik.ti.openhealthcard.events.response.callbacks.IPaceKeyResponseListener;
import de.gematik.ti.openhealthcard.events.response.entities.PaceKey;

/**
 * Its a mock of NfcCard NfcCard in NFC-Project, but not a copy of it
 */
public class NfcCardMock {
    private static final Logger LOG = LoggerFactory.getLogger(NfcCardMock.class);
    private static boolean paceKeyReceived;
    private PaceKey paceKey;
    private final ICardPresentCallBack presentCallBack;
    private final ICard card;

    /**
     * Constructor with parameter
     * @param card
     * @param presentCallBack
     */
    public NfcCardMock(final ICard card, final ICardPresentCallBack presentCallBack) {
        this.card = card;
        this.presentCallBack = presentCallBack;

        LOG.debug("!post(new RequestPaceKeyEvent");
        EventBus.getDefault().post(new RequestPaceKeyEvent(paceKeyResponseListener, card));
    }

    /**
     * create ICardPresentCallBack
     * @return
     */
    public static ICardPresentCallBack createCardPresentCallBack() {
        return new ICardPresentCallBack() {

            @java.lang.Override
            public boolean informPacekeyFinished() {
                LOG.info("CardPresentCallBack informPacekeyFinished");
                return paceKeyReceived;
            }
        };
    }

    /**
     * return paceKey
     * @return
     */
    public PaceKey getPaceKey() {
        return paceKey;
    }

    interface ICardPresentCallBack {
        boolean informPacekeyFinished();
    }

    /**
     * create IPaceKeyResponseListener
     */
    public final IPaceKeyResponseListener paceKeyResponseListener = new IPaceKeyResponseListener() {
        @Override
        public void handlePaceKey(final PaceKey paceKey) {
            LOG.debug("! RequestTransmitter().requestPaceKey(new IPaceKeyResponseListener handlePaceKey()");
            NfcCardMock.this.paceKey = paceKey;
            de.gematik.ti.epa.fdv.healthcard.authentication.NfcCardMock.this.presentCallBack.informPacekeyFinished();
            paceKeyReceived = true;
        }
    };

}
