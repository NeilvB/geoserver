    package org.geoserver.script.wfs;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;

import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionType;

import org.geoserver.script.ScriptFileWatcher;
import org.geoserver.script.ScriptManager;
import org.geoserver.wfs.TransactionEvent;
import org.geoserver.wfs.WFSException;
import org.geoserver.wfs.request.TransactionRequest;
import org.geoserver.wfs.request.TransactionResponse;
import org.geotools.util.logging.Logging;

public class ScriptTxDelegate {

    static Logger LOGGER = Logging.getLogger(ScriptTxDelegate.class);

    WfsTxHook hook;
    ScriptFileWatcher fw;

    public ScriptTxDelegate(File script, ScriptManager scriptMgr) {
        this.hook = scriptMgr.lookupWfsTxHook(script);
        this.fw = new ScriptFileWatcher(script, scriptMgr);
    }

    public TransactionType beforeTransaction(TransactionType request) throws WFSException {
        try {
            Map context = request.getExtendedProperties();
            hook.handleBefore(fw.read(), TransactionRequest.adapt(request), context);
        } 
        catch(WFSException e) {
            throw e;
        }
        catch(Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in pre transaction hook", e);
        }
        return request;
    }

    public void preInsert(TransactionEvent event) throws WFSException {
        TransactionRequest request = TransactionRequest.adapt(event.getRequest());
        Map context = request.getExtendedProperties();

        try {
            hook.handlePreInsert(fw.read(), event.getAffectedFeatures(), request, context);
        } catch (WFSException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in pre insert hook", e);
        }
    }

    public void postInsert(TransactionEvent event) throws WFSException {
        TransactionRequest request = TransactionRequest.adapt(event.getRequest());
        Map context = request.getExtendedProperties();

        try {
            hook.handlePostInsert(fw.read(), event.getAffectedFeatures(), request, context);
        } catch (WFSException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in post insert hook", e);
        }
    }

    public void preUpdate(TransactionEvent event) throws WFSException {
        TransactionRequest request = TransactionRequest.adapt(event.getRequest());
        Map context = request.getExtendedProperties();

        try {
            hook.handlePreUpdate(fw.read(), event.getAffectedFeatures(), request, context);
        } catch (WFSException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in pre update hook", e);
        }
    }

    public void postUpdate(TransactionEvent event) throws WFSException {
        TransactionRequest request = TransactionRequest.adapt(event.getRequest());
        Map context = request.getExtendedProperties();

        try {
            hook.handlePostUpdate(fw.read(), event.getAffectedFeatures(), request, context);
        } catch (WFSException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in post update hook", e);
        }
    }

    public void preDelete(TransactionEvent event) throws WFSException  {
        TransactionRequest request = TransactionRequest.adapt(event.getRequest());
        Map context = request.getExtendedProperties();

        try {
            hook.handlePreDelete(fw.read(), event.getAffectedFeatures(), request, context);
        } catch (WFSException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in pre delete hook", e);
        }
    }

    public void beforeCommit(TransactionType request) throws WFSException {
        try {
            Map context = request.getExtendedProperties();
            hook.handlePreCommit(fw.read(), TransactionRequest.adapt(request), context);
        } 
        catch(WFSException e) {
            throw e;
        }
        catch(Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in pre commit hook", e);
        }
    }

    public void afterTransaction(TransactionType request, TransactionResponseType result, boolean committed) {
        try {
            Map context = request.getExtendedProperties();

            ScriptEngine eng = fw.read();
            TransactionRequest txReq = TransactionRequest.adapt(request);
            TransactionResponse txRes = TransactionResponse.adapt(result);

            if (committed) {
                hook.handlePostCommit(eng, txReq, txRes, context);
            }
            else {
                hook.handleAbort(eng, txReq, txRes, context);
            }
        } 
        catch(Exception e) {
            LOGGER.log(Level.WARNING, "Error occured in post commit hook", e);
        }
    }
}
