/*
* Copyright 2013 Yoshio Terada
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package jp.co.oracle.samples.phaselisteners;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Yoshio Terada
 */
public class AjaxViewExpirationCheckListener implements PhaseListener {
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // NOOP.
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String originalURL = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        String loginURL = request.getContextPath() + "/faces/index.xhtml";

        if (context.getPartialViewContext().isAjaxRequest()
            && originalURL != null
            && loginURL.equals(request.getRequestURI()))
        {
            try {
                context.getExternalContext().redirect(originalURL);
            } catch (IOException e) {
                throw new FacesException(e);
            }
        }
    }

}

