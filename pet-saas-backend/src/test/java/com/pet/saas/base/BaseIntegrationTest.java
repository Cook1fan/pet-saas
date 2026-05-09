package com.pet.saas.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {"/sql/schema.sql", "/sql/data/common/base-data.sql"})
public abstract class BaseIntegrationTest {
}
