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

import de.gematik.ti.healthcard.control.common.CardFunction;
import de.gematik.ti.healthcardaccess.IHealthCard;

/**
 * Special exception on handling with ePA-Authentication
 * 
 */
public class HealthCardAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 6210949863479627937L;

    /**
     * common error-message
     * @param error
     */
    public HealthCardAuthenticationException(final String error) {
        super(error);
    }

    /**
     * Special error-message: "card " + cardHc + " isCardType not supported, required isCardType eGK2 or eGK21."
     * @param cardHc
     */
    public HealthCardAuthenticationException(final IHealthCard cardHc) {
        this(CardFunction.isEgk(cardHc) ? "Exception occured on handling with Healthcard " + cardHc
                : "card " + cardHc + " is not supported, required is eGK2 or eGK21.");
    }

}
