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

