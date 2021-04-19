create table test(
test_hash varchar(500) primary key,
test_result varchar(11) not null check(test_result in ('false', 'true')),
test_date int not null
);

create table info_person(
id varchar(500),
test_hash varchar(500),
foreign key(test_hash) references test(test_hash),
primary key (id,test_hash)
);

create table info_contacts(
id varchar(250),
id_initiator varchar (500),
duration int not null,
date int not null,
primary key(id_initiator, id, date)
);