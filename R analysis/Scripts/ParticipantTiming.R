iTiming = intervention %>% map(~.x$participant$timing) %>% as.numeric
cTiming = control %>% map(~.x$participant$timing) %>% as.numeric

x = c(iTiming, cTiming) %>% as.vector

group = c(
  rep(1, length(iTiming)),
  rep(2, length(cTiming))
)

fill = c(
  rep("Intervention", length(iTiming)),
  rep("Control", length(cTiming))
)

chart = chartDefault + 
  geom_bar(aes(x = x, group = group, fill = fill), position = position_dodge2(width = 0.9, preserve = "single")) +
  scale_x_discrete("Perceived Scare Frequency", lim = c(2:5), labels = c("Too Few", "About Right", "Too Many", "Far Too Many")) +
  labs(y = "Frequency", fill = "Group")

print(chart)
rm(iTiming, cTiming, chart, fill, group, x)