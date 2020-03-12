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

import cardfilesystem.egk21mf.df.esign.PrK;
import cardfilesystem.egk2mf.Df;
import de.gematik.ti.epa.fdv.healthcard.authentication.HealthCardAuthenticationException;
import de.gematik.ti.healthcard.control.common.CardFunction;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.Key;
import de.gematik.ti.healthcardaccess.cardobjects.PsoAlgorithm;
import de.gematik.ti.healthcardaccess.commands.ManageSecurityEnvironmentCommand;
import de.gematik.ti.healthcardaccess.commands.PsoComputeDigitalSignatureCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;

/**
 * sign a hash value, return signature
 * 
 */
public class EgkHashSigner {

    private static final PsoAlgorithm PSO_ALGO_SIGN_PSS = new PsoAlgorithm(PsoAlgorithm.Algorithm.SIGN_VERIFY_PSS);
    private static final PsoAlgorithm PSO_ALGO_SIGN_ECDSA = new PsoAlgorithm(PsoAlgorithm.Algorithm.SIGN_VERIFY_ECDSA);

    /**
     * sign plain hash value
     * @param cardHc IHealthCard
     * @param hashValue byte array
     * @return ResultOperation response
     */
    public ResultOperation<Response> signHashvalue(final IHealthCard cardHc, final byte[] hashValue) {
        return new SelectCommand(getAppIdToSelect(cardHc)).executeOn(cardHc)
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .flatMap(
                        __ -> new ManageSecurityEnvironmentCommand(ManageSecurityEnvironmentCommand.MseUseCase.KEY_SELECTION_FOR_SIGNING_KEY,
                                getPsoSignAlgorithm(cardHc),
                                getKey(cardHc), true).executeOn(cardHc))
                .validate(Response.ResponseStatus.SUCCESS::validateResult)

                .flatMap(
                        __ -> new PsoComputeDigitalSignatureCommand(getPsoSignAlgorithm(cardHc), hashValue).executeOn(cardHc))
                .validate(Response.ResponseStatus.SUCCESS::validateResult);
    }

    private PsoAlgorithm getPsoSignAlgorithm(final IHealthCard cardHc) {
        if (CardFunction.isCardType(cardHc, Egk2.class)) {
            return PSO_ALGO_SIGN_PSS;
        } else if (CardFunction.isCardType(cardHc, Egk21.class)) {
            return PSO_ALGO_SIGN_ECDSA;
        }
        throw new HealthCardAuthenticationException(cardHc);
    }

    /**
     * return PrK.ChAutR2048.KID on eGK2 or PrK.HP_AUT_E256.KID on eGK2.1 dependent on cardHc
     * @param cardHc IHealthCard
     * @return Key
     */
    private Key getKey(final IHealthCard cardHc) {
        if (CardFunction.isCardType(cardHc, Egk2.class)) {
            return new Key(cardfilesystem.egk2mf.df.esign.PrK.ChAutR2048.KID);
        } else if (CardFunction.isCardType(cardHc, Egk21.class)) {
            return new Key(PrK.ChAutE256.KID);
        }
        throw new HealthCardAuthenticationException(cardHc);
    }

    /**
     * return ESIGN.AID dependent on 'cardHc'
     * @param cardHc IHealthCard
     * @return ApplicationIdentifier
     */
    private ApplicationIdentifier getAppIdToSelect(final IHealthCard cardHc) {
        if (CardFunction.isCardType(cardHc, Egk2.class)) {
            return new ApplicationIdentifier(Df.Esign.AID);
        } else if (CardFunction.isCardType(cardHc, Egk21.class)) {
            return new ApplicationIdentifier(cardfilesystem.egk21mf.Df.Esign.AID);
        }
        throw new HealthCardAuthenticationException(cardHc);
    }
}
