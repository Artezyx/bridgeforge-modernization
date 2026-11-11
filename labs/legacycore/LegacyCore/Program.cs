using System.Net.Mail;
using System.Text.Json;
using System.Xml;
using System.Xml.Linq;

namespace LegacyCore;

public sealed record Customer(long Id, string FullName, string Email);

public static class SoapCustomerAdapter
{
    public static Customer Parse(string xml)
    {
        if (string.IsNullOrWhiteSpace(xml))
            throw new FormatException("Empty XML payload");

        var settings = new XmlReaderSettings
        {
            DtdProcessing = DtdProcessing.Prohibit,
            XmlResolver = null,
            MaxCharactersInDocument = 1024 * 1024,
            MaxCharactersFromEntities = 0,
            IgnoreComments = true
        };

        using var source = new StringReader(xml);
        using var reader = XmlReader.Create(source, settings);
        var doc = XDocument.Load(reader);
        var envelope = doc.Root;
        if (envelope?.Name.LocalName != "Envelope" ||
            envelope.Name.NamespaceName != "http://schemas.xmlsoap.org/soap/envelope/")
            throw new FormatException("Expected SOAP 1.1 Envelope");

        var body = envelope.Elements().SingleOrDefault(e => e.Name.LocalName == "Body" &&
                                                         e.Name.Namespace == envelope.Name.Namespace);
        if (body is null) throw new FormatException("SOAP Body is missing");
        if (body.Elements().Any(e => e.Name.LocalName == "Fault"))
            throw new FormatException("SOAP fault returned by legacy service");

        var customer = body.Descendants().SingleOrDefault(e => e.Name.LocalName == "Customer");
        if (customer is null) throw new FormatException("Expected exactly one Customer node");

        static string Required(XElement item, string name)
        {
            var value = item.Elements().SingleOrDefault(e => e.Name.LocalName == name)?.Value.Trim();
            if (string.IsNullOrWhiteSpace(value))
                throw new FormatException($"Missing {name}");
            return value;
        }

        if (!long.TryParse(Required(customer, "Id"), out var id) || id <= 0)
            throw new FormatException("Invalid customer ID");
        var fullName = Required(customer, "FullName");
        var email = Required(customer, "Email");
        if (fullName.Length > 150 || email.Length > 254)
            throw new FormatException("Field length exceeded");
        try
        {
            var addr = new MailAddress(email);
            if (!string.Equals(addr.Address, email, StringComparison.Ordinal))
                throw new FormatException("Malformed email");
        }
        catch (FormatException)
        {
            throw new FormatException("Malformed email");
        }
        return new Customer(id, fullName, email);
    }
}

public static class Program
{
    public static int Main(string[] args)
    {
        if (args.Length != 1)
        {
            Console.Error.WriteLine("Usage: dotnet run -- <soap-xml-file>");
            return 64;
        }
        try
        {
            var document = File.ReadAllText(args[0]);
            Console.WriteLine(JsonSerializer.Serialize(SoapCustomerAdapter.Parse(document),
                new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase }));
            return 0;
        }
        catch (Exception ex) when (ex is FormatException or XmlException or IOException or InvalidOperationException)
        {
            Console.Error.WriteLine($"SOAP parsing rejected: {ex.Message}");
            return 2;
        }
    }
}
