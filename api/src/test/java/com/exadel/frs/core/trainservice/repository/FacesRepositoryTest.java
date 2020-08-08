/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.repository;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static smile.math.MathEx.randomLong;
import com.exadel.frs.core.trainservice.entity.postgres.Face;
import com.exadel.frs.core.trainservice.repository.postgres.FacesRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class FacesRepositoryTest {

    @Autowired
    private FacesRepository facesRepository;
    private final static String MODEL_KEY = "model_key";
    private final static String MODEL_KEY_OTHER = "model_key_other";

    @BeforeEach
    void setUp() {
        val faceA = makeFace("A", MODEL_KEY);
        val faceB = makeFace("B", MODEL_KEY_OTHER);
        val faceC = makeFace("C", MODEL_KEY);

        facesRepository.saveAll(List.of(faceA, faceB, faceC));
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    public static Face makeFace(final String name, final String modelApiKey) {
        val face = new Face()
                .setFaceName(name)
                .setApiKey(modelApiKey);
        face.setFaceImg("hex-string-1".getBytes());
        face.setRawImg("hex-string-2".getBytes());
        face.setId(randomLong());
        face.setGuid(randomUUID().toString());

        return face;
    }

    @Test
    public void getAll() {
        val actual = facesRepository.findAll();

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(3);
        assertThat(actual).allSatisfy(
                face -> {
                    assertThat(face.getId()).isNotNull();
                    assertThat(face.getFaceName()).isNotEmpty();
                    assertThat(face.getApiKey()).isNotEmpty();
                    assertThat(face.getFaceImg()).isNotNull();
                    assertThat(face.getRawImg()).isNotNull();
                    assertThat(face.getGuid()).isNotNull();
                }
        );
    }

    @Test
    public void findNamesForApiGuid() {
        val expected = Arrays.asList("A", "C");
        val actual = facesRepository.findByApiKey(MODEL_KEY).stream()
                                    .map(Face::getFaceName)
                                    .collect(toList());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void countByApiKey() {
        val expected = facesRepository.findByApiKey(MODEL_KEY);
        val actual = facesRepository.countByApiKey(MODEL_KEY);

        assertThat(actual).isGreaterThan(0);
        assertThat(actual).isEqualTo(expected.size());
    }

    @Test
    public void findByGuid() {
        val faces = facesRepository.findAll();
        val face = faces.get(Math.abs(new Random().nextInt()) % faces.size());

        val actual = facesRepository.findByGuid(face.getGuid());
        assertTrue(actual.isPresent());
        assertEquals(actual.get().getGuid(), face.getGuid());
    }
}