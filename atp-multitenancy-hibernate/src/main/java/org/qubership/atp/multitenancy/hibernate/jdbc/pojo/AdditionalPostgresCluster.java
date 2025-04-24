/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.multitenancy.hibernate.jdbc.pojo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Setter;

@Component
@Setter
public class AdditionalPostgresCluster extends DefaultPostgresCluster {

    private String projects;

    /**
     * Splits projects by comma.
     *
     * @return list of projects
     */
    public List<String> getProjectsAsList() {
        List<String> formatted = new ArrayList<>();
        String[] stringProjects = this.projects.split(",");
        for (String project : stringProjects) {
            formatted.add(project.trim());
        }
        return formatted;
    }
}
