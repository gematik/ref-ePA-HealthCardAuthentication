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

import de.gematik.ti.epa.fdv.authentication.service.provider.api.IAuthenticator;
import de.gematik.ti.epa.fdv.authentication.service.provider.spi.IAuthenticationProvider;
import de.gematik.ti.epa.fdv.healthcard.authentication.Authenticator;

/**
 * Provider of Authentication
 */
public class AuthenticationProvider implements IAuthenticationProvider {
    /**
     * get a IAuthenticator
     * @return IAuthenticator
     */
    @Override
    public IAuthenticator getDefaultAuthenticator() {
        return new Authenticator();
    }

    /**
     * description of this provider
     * @return String
     */
    @Override
    public String getDescription() {
        return "gematik-Authenticator with gematik-eGK from version 2";
    }
}
