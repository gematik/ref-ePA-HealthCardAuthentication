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
import org.greenrobot.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.epa.fdv.authentication.service.provider.api.ICertificateHolder;
import de.gematik.ti.healthcard.control.common.event.absent.AbstractHealthCardAbsentEvent;
import de.gematik.ti.healthcard.control.common.event.present.AbstractHealthCardPresentEvent;
import de.gematik.ti.healthcard.control.common.pace.TrustedChannelPaceKeyRequestHandler;
import de.gematik.ti.healthcardaccess.IHealthCard;

/**
 * Find the available Card for FdV
 */
public class AuthenticationController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);
    private IHealthCard healthCard;

    /**
     * start TrustedChannelPaceKeyRequestHandler and register the AuthenticationController in EventBus
     * @return AuthenticationController
     */
    public AuthenticationController start() {
        LOG.debug("register " + this);
        EventBus.getDefault().register(this);
        if (!EventBus.getDefault().isRegistered(TrustedChannelPaceKeyRequestHandler.getInstance())) {
            TrustedChannelPaceKeyRequestHandler.startHandling();
        }
        return this;
    }

    /**
     * handle coming CardAbsentEvent
     * @param healthCardEvent AbstractHealthCardAbsentEvent
     */
    @Subscribe
    public void handleCardAbsentEvents(final AbstractHealthCardAbsentEvent healthCardEvent) {
    }

    /**
     * handle coming CardPresentEvent
     * @param healthCardEvent AbstractHealthCardPresentEvent
     */
    @Subscribe
    public void handleCardPresentEvents(final AbstractHealthCardPresentEvent healthCardEvent) {
        healthCard = healthCardEvent.getHealthCard();
    }

    /**
     * Get the health card
     *
     * @return Map with key of specific HealthCard and value of representing card reader
     */
    private IHealthCard getHealthCard() {
        if (healthCard != null) {
            return healthCard;
        }
        throw new HealthCardAuthenticationException("No Card available.");
    }

    /**
     * Stop TrustedChannelPaceKeyRequestHandler and unregister EventBus
     */
    public void stop() {
        LOG.debug("unregister " + this);
        EventBus.getDefault().unregister(this);
        TrustedChannelPaceKeyRequestHandler.stopHandling();
    }

    /**
     * get ICertificateHolder which has certificate
     * @return ICertificateHolder
     */
    public ICertificateHolder<IHealthCard> getCertificateHolder() {

        return (ICertificateHolder) () -> getHealthCard();
    }
}
