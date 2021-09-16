SELECT
	*
FROM
	`mall`.`t_config`
WHERE
	CAST(
		`alliance`.`t_config`.`name` AS CHAR CHARACTER
		SET utf8
	) COLLATE utf8_general_ci LIKE '%https://%'
OR CAST(
	`alliance`.`t_config`.`key_name` AS CHAR CHARACTER
	SET utf8
) COLLATE utf8_general_ci LIKE '%https://%'
OR CAST(
	`alliance`.`t_config`.`value_type` AS CHAR CHARACTER
	SET utf8
) COLLATE utf8_general_ci LIKE '%https://%'
OR CAST(
	`alliance`.`t_config`.`value` AS CHAR CHARACTER
	SET utf8
) COLLATE utf8_general_ci LIKE '%https://%'
OR CAST(
	`alliance`.`t_config`.`type` AS CHAR CHARACTER
	SET utf8
) COLLATE utf8_general_ci LIKE '%https://%'
OR CAST(
	`alliance`.`t_config`.`description` AS CHAR CHARACTER
	SET utf8
) COLLATE utf8_general_ci LIKE '%https://%';
