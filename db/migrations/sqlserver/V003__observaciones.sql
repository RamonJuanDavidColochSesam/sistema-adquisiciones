IF COL_LENGTH('OrdenCompra','observaciones') IS NULL ALTER TABLE OrdenCompra ADD observaciones VARCHAR(300) NULL;
IF COL_LENGTH('Oferta','observaciones') IS NULL ALTER TABLE Oferta ADD observaciones VARCHAR(300) NULL;
IF COL_LENGTH('Adjudicacion','observaciones') IS NULL ALTER TABLE Adjudicacion ADD observaciones VARCHAR(300) NULL;
