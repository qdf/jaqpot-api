/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service.client.jpdi;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Future;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Report;
import org.jaqpot.core.model.dto.dataset.Dataset;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
public interface JPDIClient extends Closeable {

    public Future<Model> train(Dataset dataset, Algorithm algorithm, Map<String, Object> parameters, String predictionFeature, MetaInfo modelMeta, String taskId);

    public Future<Dataset> predict(Dataset dataset, Model model, MetaInfo datasetMeta, String taskId);

    public Future<Dataset> transform(Dataset dataset, Algorithm algorithm, Map<String, Object> parameters, String predictionFeature, MetaInfo datasetMeta, String taskId);

    public Future<Report> report(Dataset dataset, Algorithm algorithm, Map<String, Object> parameters, MetaInfo reportMeta, String taskId);

    public boolean cancel(String taskId);
}
