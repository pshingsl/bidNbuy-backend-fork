create database bidnbuy default character set utf8 default collate utf8_general_ci;

--use bidnbuy;

create table Admin(
    admin_id BIGINT auto_increment not null PRIMARY KEY,
    email VARCHAR(255) not null unique,
    password VARCHAR(100) not null,
    name VARCHAR(20) not null,
    ip_address VARCHAR(100) not null,
    create_at DATETIME DEFAULT  CURRENT_TIMESTAMP not null,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

create table Address(
    address_id BIGINT not null auto_increment PRIMARY KEY,
    zonecode VARCHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    address_type CHAR(1) NOT NULL,
    detail_address VARCHAR(255) NULL,
    created_at DATETIME DEFAULT  CURRENT_TIMESTAMP not null,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

create table User(
    user_id BIGINT auto_increment not null PRIMARY KEY,
    admin_id BIGINT,
    address_id BIGINT not null,
    email VARCHAR(255) not null unique,
    password VARCHAR(100) not null,
    nickname VARCHAR(20) not null,
    auth_status ENUM('Y', 'N', 'P') default 'N' not null,
    user_status ENUM('Y', 'N', 'B') default 'Y' not null,
    user_type VARCHAR(20),
    create_at DATETIME DEFAULT  CURRENT_TIMESTAMP not null,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id),
    FOREIGN KEY (address_id) REFERENCES Address(address_id)
);

create table EmailVerification(
    email_id BIGINT  auto_increment not null PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255) not null,
    valid_code varchar(50) not null,
    expiration_time TIMESTAMP,
    is_verified ENUM('Y', 'N', 'P') default 'N' not null,
    create_at DATETIME DEFAULT  CURRENT_TIMESTAMP not null,

    FOREIGN KEY (user_id) REFERENCES User(user_id)
);

create table RefreshToken (
    token_id     BIGINT       auto_increment not null PRIMARY KEY,
    user_id      BIGINT       not null UNIQUE, --User table FK
    token_value  VARCHAR(500) not null,
    expiry_date  TIMESTAMP    not null,

    FOREIGN KEY (user_id) REFERENCES User(user_id)
);
