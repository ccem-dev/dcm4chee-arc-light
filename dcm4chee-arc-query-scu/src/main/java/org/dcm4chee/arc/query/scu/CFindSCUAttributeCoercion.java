/*
 * *** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * *** END LICENSE BLOCK *****
 */

package org.dcm4chee.arc.query.scu;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.AttributesCoercion;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Priority;
import org.dcm4chee.arc.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since May 2016
 */
public class CFindSCUAttributeCoercion implements AttributesCoercion {

    private final static Logger LOG = LoggerFactory.getLogger(CFindSCUAttributeCoercion.class);

    private final ApplicationEntity localAE;
    private final String calledAET;
    private final int[] returnKeys;
    private final Attributes.UpdatePolicy attributeUpdatePolicy;
    private final CFindSCU cfindSCU;
    private final Cache<String,Attributes> queryCache;
    private final AttributesCoercion next;

    public CFindSCUAttributeCoercion(
            ApplicationEntity localAE, String calledAET, int[] returnKeys,
            Attributes.UpdatePolicy attributeUpdatePolicy, CFindSCU cfindSCU, Cache<String, Attributes> queryCache,
            AttributesCoercion next) {
        this.localAE = localAE;
        this.calledAET = calledAET;
        this.returnKeys = returnKeys;
        this.attributeUpdatePolicy = attributeUpdatePolicy;
        this.cfindSCU = cfindSCU;
        this.queryCache = queryCache;
        this.next = next;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) {
        String studyIUID = attrs.getString(Tag.StudyInstanceUID);
        Attributes newAttrs = cfindSCU.queryStudy(localAE, calledAET, Priority.NORMAL, studyIUID, returnKeys, queryCache);
        if (newAttrs != null)
            attrs.update(attributeUpdatePolicy, newAttrs, modified);
        else
            LOG.warn("Failed to query Study[{}] from {} - do not coerce attributes", studyIUID, calledAET);
        if (next != null)
            next.coerce(attrs, modified);
    }
}
