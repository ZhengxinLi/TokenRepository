redis.replicate_commands()
local result = -1
local rate_limit_info = redis.pcall("HMGET", KEYS[1], "last_mill_second", "current_permits", "max_burst", "rate", "key")
local last_mill_second = rate_limit_info[1]
local current_permits = tonumber(rate_limit_info[2])
local redisTime = redis.pcall('TIME')
local currentMills = last_mill_second
if last_mill_second == nil then
    currentMills = ARGV[1]
end
if (redisTime ~= nil) then
    currentMills = tonumber((redisTime[1] * 1000000 + redisTime[2])) / 1000
end
if current_permits ~= nil and last_mill_second ~= nil and current_permits < tonumber(ARGV[2]) then
    redis.call("HMSET", KEYS[1], "last_mill_second", last_mill_second, "current_permits", current_permits, "max_burst", ARGV[3], "rate", ARGV[4], "key", ARGV[5])
    result = 1
else
    redis.call("HMSET", KEYS[1], "last_mill_second", currentMills, "current_permits", ARGV[2], "max_burst", ARGV[3], "rate", ARGV[4], "key", ARGV[5])
    result = 1
end
return result
