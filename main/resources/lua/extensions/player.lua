--[[
	Please do not edit this file, it will get updated on startup
	If you want a function to be added here, post it on the forums
	
	Thanks!
]]

local Player = FindMetaTable( "Player" )

local pdata = sql.Connect( ("jdbc:sqlite:luacraft/%s.db"):format( SERVER and "sv" or "cl" ) )
if pdata then
	pdata:Query( "CREATE TABLE IF NOT EXISTS playerpdata ( infoid TEXT NOT NULL PRIMARY KEY, value TEXT )" ):Start()

	function Player:GetPData( name, func )
		local query = pdata:Query( "SELECT value FROM playerpdata WHERE infoid = ?[?] LIMIT 1", func )
		query:SetString( 1, self:GetUniqueID() )
		query:SetString( 2, name )
		query:Start()
	end

	function Player:SetPData( name, value )
		local query = pdata:Query( "REPLACE INTO playerpdata ( infoid, value ) VALUES (?[?],?)" )
		query:SetString( 1, self:GetUniqueID() )
		query:SetString( 2, name )
		query:SetString( 3, value )
		query:Start()
	end

	function Player:RemovePData( name )
		local query = pdata:Query( "DELETE FROM playerpdata WHERE infoid = ?[?]" )
		query:SetString( 1, self:GetUniqueID() )
		query:SetString( 2, name )
		query:Start()
	end
else
    console.warn( ("Failed to open to sqlite:%s.sb, PData will be unavailable"):format( SERVER and "sv" or "cl" ) )
end

if SERVER then	
	function Player:SendLua( lua )
		local buff = net.Start( "SendLua" )
			buff:WriteString( lua )
		buff:Send( self )
	end
else
	net.Receive( "SendLua", function( buff )
		local func, err = loadstring( buff:ReadString(), "SendLua" )
		assert( func, err )
		local status, err = pcall( func )
		assert( status, err )
	end )
end