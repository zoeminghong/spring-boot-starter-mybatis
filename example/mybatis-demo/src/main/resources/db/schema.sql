CREATE TABLE `t_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(20) NOT NULL,
  `role_name` varchar(40) NOT NULL DEFAULT '',
  `app_prop_id` int(11) NOT NULL ,
  `created_by` int(11) NOT NULL,
  `created_time` timestamp NOT NULL,
  `updated_by` int(11) NOT NULL,
  `updated_time` timestamp NOT NULL,
  `del_flag` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
) AUTO_INCREMENT=0;