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

package de.gematik.ti.epa.fdv.healthcard.authentication.healthcard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cardfilesystem.Egk21FileSystem;
import cardfilesystem.Egk2FileSystem;
import de.gematik.ti.epa.fdv.healthcard.authentication.HealthCardAuthenticationException;
import de.gematik.ti.healthcard.control.common.CardFunction;
import de.gematik.ti.healthcard.control.common.integration.card.ReadCommandSafeExecutor;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.FileIdentifier;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;

/**
 * load certificates from healthCard
 */
public class EgkCertificateLoader {

    private static final Logger LOG = LoggerFactory.getLogger(EgkCertificateLoader.class);

    /**
     * load certificate in byte array
     * @param cardToRead IHealthCard
     * @return ResultOperation byte[]
     */
    public ResultOperation<byte[]> loadCertificate(final IHealthCard cardToRead) {
        final ReadCommandSafeExecutor readCommandSafeExecutorEgk2 = new ReadCommandSafeExecutor(cardToRead,
                cardToRead.getCurrentCardChannel().getMaxResponseLength());
        return readCommandSafeExecutorEgk2.readSafe(getAppIdToSelect(cardToRead), getFidToReadCertificate(cardToRead));
    }

    private FileIdentifier getFidToReadCertificate(final IHealthCard cardHc) {
        if (CardFunction.isCardType(cardHc, Egk2.class)) {
            return new FileIdentifier(cardfilesystem.egk2mf.df.esign.Ef.CchAutR2048.FID);
        }
        if (CardFunction.isCardType(cardHc, Egk21.class)) {
            return new FileIdentifier(cardfilesystem.egk21mf.df.esign.Ef.C_CH_AUT_E256.FID);
        }
        throw new HealthCardAuthenticationException(cardHc);
    }

    /**
     * return ESIGN.AID
     * @param cardHc IHealthCard
     * @return ApplicationIdentifier
     */
    private ApplicationIdentifier getAppIdToSelect(final IHealthCard cardHc) {
        if (CardFunction.isCardType(cardHc, Egk2.class)) {
            return new ApplicationIdentifier(Egk2FileSystem.DF.ESIGN.AID);
        } else if (CardFunction.isCardType(cardHc, Egk21.class)) {
            return new ApplicationIdentifier(Egk21FileSystem.DF.ESIGN.AID);
        }
        throw new HealthCardAuthenticationException(cardHc);
    }

}
