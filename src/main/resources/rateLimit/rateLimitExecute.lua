redis.replicate_commands()
local result = -1
local redisTime = redis.pcall('TIME')
local rate_limit_info = redis.pcall("HMGET", KEYS[1], "last_mill_second", "current_permits", "max_burst", "rate", "key")
local last_mill_second = rate_limit_info[1]
local current_permits = tonumber(rate_limit_info[2])
local max_burst = tonumber(rate_limit_info[3])
local rate = tonumber(rate_limit_info[4])
local key = tostring(rate_limit_info[5])
local currentMills = last_mill_second
if key == nil then
    return result
end
if (redisTime ~= nil) then
    currentMills = tonumber((redisTime[1] * 1000000 + redisTime[2])) / 1000
end
local local_current_permits = max_burst;
if (type(last_mill_second) ~= 'boolean' and last_mill_second ~= nil) then
    local reverse_permits = math.floor((currentMills - last_mill_second) / 1000) * rate
    if (reverse_permits > 0) then
        redis.pcall("HMSET", KEYS[1], "last_mill_second", currentMills)
    end
    local expect_current_permits = reverse_permits + current_permits
    local_current_permits = math.min(expect_current_permits, max_burst);
else
    redis.pcall("HMSET", KEYS[1], "last_mill_second", currentMills)
end
if (local_current_permits - ARGV[1] >= 0) then
    result = 1
    redis.pcall("HMSET", KEYS[1], "current_permits", local_current_permits - ARGV[1])
else
    redis.pcall("HMSET", KEYS[1], "current_permits", local_current_permits)
end
return result