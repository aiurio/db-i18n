create table T_I18N_HORIZONTAL(
    CODE varchar2(100) not null,
    EN   varchar2(255) null,
    EN_US varchar(255) null,
    DE   varchar2(255) null,
    RU   varchar2(255) null
);

alter table T_I18N_HORIZONTAL
  add constraint PK_T_I18N_HORIZONTAL primary key (CODE);

alter table T_I18N_HORIZONTAL
  add constraint U_T_I18N_HORIZONTAL unique (CODE);

insert into T_I18N_HORIZONTAL values (
    'app.startup.successful', 
        'Application started.', 
        'Yo app started.',
        'Applikation gestartet.', 
        'Приложение запущено.'
);