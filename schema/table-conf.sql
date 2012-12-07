


CREATE TABLE `item` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `item` ADD COLUMN `token` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `item` ADD UNIQUE KEY `unique_token` (`token`);
ALTER TABLE `item` ADD COLUMN `type` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `item` ADD INDEX `index_type` (`type`);
ALTER TABLE `item` ADD COLUMN `label` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `item` ADD COLUMN `price` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `item` ADD COLUMN `location` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `item` ADD COLUMN `position` VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE `item` ADD COLUMN `description` TEXT DEFAULT '' NOT NULL DEFAULT '';
ALTER TABLE `item` ADD COLUMN `startTime` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `item` ADD COLUMN `endTime` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `item` ADD COLUMN `created` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `item` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `item` ADD INDEX `index_user` (`user`);
ALTER TABLE `item` ADD COLUMN `facebookIdentity` INT(11)  ;
ALTER TABLE `item` ADD INDEX `index_facebookIdentity` (`facebookIdentity`);
ALTER TABLE `item` ADD COLUMN `twitterIdentity` INT(11)  ;
ALTER TABLE `item` ADD INDEX `index_twitterIdentity` (`twitterIdentity`);



CREATE TABLE `comment` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `comment` ADD COLUMN `key` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `comment` ADD INDEX `index_key` (`key`);
ALTER TABLE `comment` ADD COLUMN `rank` DOUBLE NOT NULL DEFAULT 0;
ALTER TABLE `comment` ADD COLUMN `created` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `comment` ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `comment` ADD COLUMN `comment` TEXT DEFAULT '' NOT NULL DEFAULT '';
ALTER TABLE `comment` ADD COLUMN `facebookPost` VARCHAR(255)  ;
ALTER TABLE `comment` ADD COLUMN `twitterPost` VARCHAR(255)  ;
ALTER TABLE `comment` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `comment` ADD INDEX `index_user` (`user`);
ALTER TABLE `comment` ADD COLUMN `facebookIdentity` INT(11)  ;
ALTER TABLE `comment` ADD INDEX `index_facebookIdentity` (`facebookIdentity`);
ALTER TABLE `comment` ADD COLUMN `twitterIdentity` INT(11)  ;
ALTER TABLE `comment` ADD INDEX `index_twitterIdentity` (`twitterIdentity`);
ALTER TABLE `comment` ADD INDEX `index_keyRank` (`key`,`rank`);




CREATE TABLE `undefineduser` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;




CREATE TABLE `user` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `user` ADD COLUMN `token` VARCHAR(255)  ;
ALTER TABLE `user` ADD UNIQUE KEY `unique_token` (`token`);
ALTER TABLE `user` ADD COLUMN `name` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `status` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `email` VARCHAR(255)  ;
ALTER TABLE `user` ADD UNIQUE KEY `unique_email` (`email`);
ALTER TABLE `user` ADD COLUMN `pendingEmail` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `password` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `isAdmin` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `user` ADD COLUMN `facebookIdentity` INT(11)  ;
ALTER TABLE `user` ADD UNIQUE KEY `unique_facebookIdentity` (`facebookIdentity`);
ALTER TABLE `user` ADD COLUMN `twitterIdentity` INT(11)  ;
ALTER TABLE `user` ADD UNIQUE KEY `unique_twitterIdentity` (`twitterIdentity`);



CREATE TABLE `searchtoken` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `searchtoken` ADD COLUMN `item` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `searchtoken` ADD COLUMN `token` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `searchtoken` ADD COLUMN `geopoint` VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE `searchtoken` ADD COLUMN `age` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `searchtoken` ADD COLUMN `score` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `searchtoken` ADD UNIQUE KEY `unique_uniqueToken` (`item`,`token`);

ALTER TABLE `searchtoken` ADD INDEX `index_tokenAgeGeoScore` (`token`,`age`,`geopoint`,`score`);




CREATE TABLE `itemphoto` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `itemphoto` ADD COLUMN `item` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD INDEX `index_item` (`item`);
ALTER TABLE `itemphoto` ADD COLUMN `photo` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemphoto` ADD COLUMN `photoName` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemphoto` ADD COLUMN `photoExt` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemphoto` ADD COLUMN `caption` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemphoto` ADD COLUMN `photoState` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `bgState` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `thumbState` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `scaledState` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `width` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `height` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `rgb` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD COLUMN `rgbString` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemphoto` ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `itemphoto` ADD UNIQUE KEY `unique_uniquePhoto` (`item`,`photo`);




CREATE TABLE `itemproperty` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `itemproperty` ADD COLUMN `item` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemproperty` ADD INDEX `index_item` (`item`);
ALTER TABLE `itemproperty` ADD COLUMN `namespace` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemproperty` ADD INDEX `index_namespace` (`namespace`);
ALTER TABLE `itemproperty` ADD COLUMN `property` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemproperty` ADD COLUMN `value` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemproperty` ADD UNIQUE KEY `unique_uniqueNamespaceProperty` (`item`,`namespace`,`property`);




CREATE TABLE `itemcomment` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `itemcomment` ADD COLUMN `item` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemcomment` ADD INDEX `index_item` (`item`);
ALTER TABLE `itemcomment` ADD COLUMN `rank` DOUBLE NOT NULL DEFAULT 0;
ALTER TABLE `itemcomment` ADD COLUMN `created` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `itemcomment` ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `itemcomment` ADD COLUMN `comment` TEXT DEFAULT '' NOT NULL DEFAULT '';
ALTER TABLE `itemcomment` ADD COLUMN `facebookPost` VARCHAR(255)  ;
ALTER TABLE `itemcomment` ADD COLUMN `twitterPost` VARCHAR(255)  ;
ALTER TABLE `itemcomment` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemcomment` ADD INDEX `index_user` (`user`);
ALTER TABLE `itemcomment` ADD COLUMN `facebookIdentity` INT(11)  ;
ALTER TABLE `itemcomment` ADD INDEX `index_facebookIdentity` (`facebookIdentity`);
ALTER TABLE `itemcomment` ADD COLUMN `twitterIdentity` INT(11)  ;
ALTER TABLE `itemcomment` ADD INDEX `index_twitterIdentity` (`twitterIdentity`);
ALTER TABLE `itemcomment` ADD INDEX `index_itemRank` (`item`,`rank`);




CREATE TABLE `usertoken` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `usertoken` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `usertoken` ADD INDEX `index_user` (`user`);
ALTER TABLE `usertoken` ADD COLUMN `key` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `usertoken` ADD INDEX `index_key` (`key`);
ALTER TABLE `usertoken` ADD COLUMN `hash` VARCHAR(255) NOT NULL DEFAULT '';



CREATE TABLE `itemtag` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `itemtag` ADD COLUMN `item` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `itemtag` ADD INDEX `index_item` (`item`);
ALTER TABLE `itemtag` ADD COLUMN `tag` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `itemtag` ADD INDEX `index_tag` (`tag`);
ALTER TABLE `itemtag` ADD UNIQUE KEY `unique_uniqueTag` (`item`,`tag`);




CREATE TABLE `identity` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `identity` ADD COLUMN `type` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `identity` ADD COLUMN `token` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `identity` ADD COLUMN `secret` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `identity` ADD COLUMN `user` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `identity` ADD INDEX `index_user` (`user`);
ALTER TABLE `identity` ADD COLUMN `name` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `identity` ADD COLUMN `url` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `identity` ADD COLUMN `avatar` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `identity` ADD UNIQUE KEY `unique_uniqueTypeUser` (`type`,`user`);




CREATE TABLE `betauser` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `betauser` ADD COLUMN `email` VARCHAR(255) NOT NULL DEFAULT '';
