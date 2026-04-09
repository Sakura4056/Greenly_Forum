# =================================================================
# Greenly SQL 脚本拆分工具
# 功能: 将 greenly-init.sql 拆分为表结构、索引和数据文件
# =================================================================

$sourceFile = "plant-backend\src\main\resources\db\greenly-init.sql"
$tablesDir = "docs-archive\db-scripts\tables"
$indexesDir = "docs-archive\db-scripts\indexes"
$dataDir = "docs-archive\db-scripts\data"

Write-Host "🔍 开始解析 SQL 文件..." -ForegroundColor Cyan

$content = Get-Content $sourceFile -Raw -Encoding UTF8

# 提取所有表名
$tablePattern = 'CREATE TABLE `(\w+)`'
$tableMatches = [regex]::Matches($content, $tablePattern)
$tableNames = $tableMatches | ForEach-Object { $_.Groups[1].Value }

Write-Host "✅ 发现 $($tableNames.Count) 张表: $($tableNames -join ', ')" -ForegroundColor Green

# 分割SQL内容为不同的部分
$sections = $content -split '-- ----------------------------'

foreach ($section in $sections) {
    # 检测是否为表定义
    if ($section -match 'CREATE TABLE `(\w+)`') {
        $tableName = $matches[1]
        
        # 提取表注释（用途）
        $commentMatch = [regex]::Match($section, '-- 用途:\s*(.+)')
        $tableComment = if ($commentMatch.Success) { $commentMatch.Groups[1].Value.Trim() } else { "" }
        
        # ========== 提取表结构（不含索引）==========
        $createTableMatch = [regex]::Match($section, '(DROP TABLE IF EXISTS.*?;\s*CREATE TABLE.*?PRIMARY KEY \([^)]+\))', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        
        if ($createTableMatch.Success) {
            $tableStructure = $createTableMatch.Groups[1].Value
            
            # 移除索引定义（保留 PRIMARY KEY）
            $tableOnly = $tableStructure -replace '\s*(UNIQUE KEY|KEY)\s+`[^`]+`\s*\([^)]+\)[,\s]*', ''
            
            $tableHeader = @"
-- =================================================================
-- Greenly 植物养护管理系统 - $tableName 表结构
-- 用途: $tableComment
-- =================================================================

"@
            
            $tableOnly + "`n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='$tableComment';" | 
                Set-Content "$tablesDir\$tableName.sql" -Encoding UTF8 -NoNewline
            
            Write-Host "  ✅ 表结构: $tableName.sql" -ForegroundColor Gray
        }
        
        # ========== 提取索引定义 ==========
        $indexMatches = [regex]::Matches($section, '(UNIQUE KEY|KEY)\s+`([^`]+)`\s*\(([^)]+)\)', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        
        if ($indexMatches.Count -gt 0) {
            $indexHeader = @"
-- =================================================================
-- Greenly 植物养护管理系统 - $tableName 索引定义
-- =================================================================

ALTER TABLE `$tableName`

"@
            
            $indexStatements = @()
            foreach ($idxMatch in $indexMatches) {
                $keyType = $idxMatch.Groups[1].Value
                $keyName = $idxMatch.Groups[2].Value
                $keyColumns = $idxMatch.Groups[3].Value
                
                $indexStatements += "ADD $keyType `$keyName` ($keyColumns)"
            }
            
            $indexSql = $indexHeader + ($indexStatements -join ",`n") + ";`n"
            $indexSql | Set-Content "$indexesDir\$tableName`_indexes.sql" -Encoding UTF8 -NoNewline
            
            Write-Host "  ✅ 索引定义: ${tableName}_indexes.sql ($($indexMatches.Count) 个索引)" -ForegroundColor Gray
        }
    }
    
    # 检测是否为数据插入
    if ($section -match 'INSERT INTO `(\w+)`') {
        $insertMatches = [regex]::Matches($section, 'INSERT INTO `(\w+)`.+?;', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        
        foreach ($insertMatch in $insertMatches) {
            $insertSql = $insertMatch.Value
            $dataTableName = [regex]::Match($insertSql, 'INSERT INTO `(\w+)`').Groups[1].Value
            
            $dataHeader = @"
-- =================================================================
-- Greenly 植物养护管理系统 - $dataTableName 初始数据
-- =================================================================

"@
            
            # 追加到对应的数据文件
            $dataFile = "$dataDir\$dataTableName`_data.sql"
            
            if (-not (Test-Path $dataFile)) {
                $dataHeader | Set-Content $dataFile -Encoding UTF8 -NoNewline
            }
            
            "`n$insertSql" | Add-Content $dataFile -Encoding UTF8 -NoNewline
        }
    }
}

Write-Host "`n🎉 SQL 拆分完成！" -ForegroundColor Green
Write-Host "  📁 表结构文件: $(Get-ChildItem $tablesDir -Filter '*.sql').Count 个" -ForegroundColor Cyan
Write-Host "  📁 索引文件: $(Get-ChildItem $indexesDir -Filter '*.sql').Count 个" -ForegroundColor Cyan  
Write-Host "  📁 数据文件: $(Get-ChildItem $dataDir -Filter '*.sql').Count 个" -ForegroundColor Cyan
