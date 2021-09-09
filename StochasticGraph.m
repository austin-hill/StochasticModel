files = ["VOO"];
for i = 1:length(files)
    name = files(i);
    predictedPricesName = strcat(files(i), "PredictedPrices.data");
    statsName = strcat(files(i), "Stats.data");
    predictedPrices = load(predictedPricesName);
    stats = load(statsName);
    n = stats(1, 1);
    y = stats(2, 1);
    m = stats(3, 1);
    d = stats(4, 1);
    k = stats(5, 1);
    
    ARPE = zeros(n, 1);
    for j = 1:n
        sum = 0;
        for i = 1:730
            sum = sum + abs(predictedPrices(k+i, j) - predictedPrices(k+i, n+1))/predictedPrices(k+i, n+1);
        end
        ARPE(j) = 100*sum/730;
    end

    T = strcat(name);
    t = datetime(y, m, d) + caldays(0:k+730);
    
    hold on
    title(T)
    xlabel('Time (days)')
    ylabel('Price (USD)')
    predictedSum = zeros(1, k+731);
    counter = 0;

    for i = 1:n
        plot(t, predictedPrices(:, i), ':b')
    end
    plot(t, predictedPrices(:, n + 1), 'r')
    orient landscape
    savefig(name)
    clf
end
disp(ARPE)