-- Create kana table if not exists
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='kana' AND xtype='U')
BEGIN
    CREATE TABLE kana (
        id INT PRIMARY KEY IDENTITY(1,1),
        character NVARCHAR(10) NOT NULL,
        romaji NVARCHAR(20) NOT NULL,
        type NVARCHAR(10) NOT NULL -- 'hiragana', 'katakana', 'gojuuon', 'dakuon', 'youon'
    );
    PRINT 'Table kana created successfully.';
END
ELSE
BEGIN
    PRINT 'Table kana already exists.';
END

