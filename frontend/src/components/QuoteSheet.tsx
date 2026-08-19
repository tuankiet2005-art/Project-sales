import type { ReactNode } from "react";
import type { Lang } from "../i18n/translations";
import { formatQuoteAmount } from "../lib/format";
import { translateQuoteLabel } from "../lib/quoteLabels";
import { colorHex, colorPhoto, paintLabelClass } from "../lib/vehicleColor";
import type { AccessoryItem, CostBreakdown, VehicleDetail } from "../types";

const DEALER_NAME_LINES = ["MITSUBISHI MOVEO NEW CITY", "THÀNH PHỐ MỚI BÌNH DƯƠNG"];
const DEALER_ADDRESS_LINES = ["1C, Đường Hùng Vương, Phường Hòa Phú", "TP. Thủ Dầu Một, Tỉnh Bình Dương"];
const QUOTE_VALIDITY = "Hiệu lực báo giá áp dụng trong tháng";
const BANK_NOTE =
  "Quý khách đặt cọc và ký hợp đồng chúng tôi sẽ tiến hành thẩm định và làm hồ sơ ngân hàng, trường hợp ngân hàng không đồng ý cho vay, chúng tôi sẽ hoàn lại 100% tiền cọc";
const LOAN_YEARS = 5;
const LOAN_ANNUAL_RATE = 0.078;

function feeAmount(result: CostBreakdown, code: string): number {
  const fee = result.fees.find((item) => item.code === code);
  if (!fee || !fee.includedInTotal) {
    return 0;
  }
  return Number(fee.amount) || 0;
}

function money(amount: number, blankIfZero = false): string {
  if (blankIfZero && !amount) {
    return "";
  }
  return formatQuoteAmount(amount);
}

function todayLabel(): string {
  return new Date().toLocaleDateString("vi-VN");
}

function giftPairs(gifts?: string): string[][] {
  const items = (gifts ?? "")
    .split(/[;|]/)
    .map((item) => item.trim())
    .filter(Boolean);
  const pairs: string[][] = [];
  for (let i = 0; i < items.length; i += 2) {
    pairs.push([items[i], items[i + 1] ?? ""]);
  }
  while (pairs.length < 6) {
    pairs.push(["", ""]);
  }
  return pairs;
}

function colors(vehicle: VehicleDetail): string[] {
  return (vehicle.availableColors ?? vehicle.defaultColor ?? "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export function QuoteSheet({
  vehicle,
  result,
  customerName,
  customerAddress,
  color,
  selectedAccessories = [],
  language = "vi",
}: {
  vehicle: VehicleDetail;
  result: CostBreakdown;
  customerName: string;
  customerAddress: string;
  color: string;
  selectedAccessories?: AccessoryItem[];
  language?: Lang;
}) {
  const tr = (text: string) => translateQuoteLabel(text, language);
  const listPrice = Number(result.listPrice) || 0;
  const discount = Number(result.discountAmount) || 0;
  const salePrice = Number(result.salePrice ?? vehicle.salePrice ?? listPrice - discount);
  const registrationTax = feeAmount(result, "REGISTRATION_TAX");
  const licensePlate = feeAmount(result, "LICENSE_PLATE");
  const inspection = feeAmount(result, "INSPECTION");
  const insurance = feeAmount(result, "COMPULSORY_INSURANCE");
  const roadUse = feeAmount(result, "ROAD_USE");
  const optionalBody = feeAmount(result, "OPTIONAL_BODY_INSURANCE");
  const mica = feeAmount(result, "MICA_PLATE");
  const registrationService = feeAmount(result, "REGISTRATION_SERVICE") || feeAmount(result, "REGISTRATION_FEE");
  const registrationTotal = Number(result.totalMandatoryFees) + Number(result.totalOptionalFees);
  const extrasTotal = Number(result.accessoriesTotal) || 0;
  const accessories = result.accessories ?? selectedAccessories;
  const firstAccessory = accessories[0];
  const extraAccessory = firstAccessory
    ? `${firstAccessory.name} (${formatQuoteAmount(Number(firstAccessory.amount))})`
    : tr("Phụ kiện trang bị thêm (Nếu có)");
  const onRoadTotal = Number(result.estimatedOnRoadTotal);
  const deposit = Number(result.deposit ?? vehicle.defaultDeposit) || 0;
  const cashSecond = Math.max(onRoadTotal - deposit, 0);
  const loanAmount = Math.max(salePrice - deposit, 0);
  const months = LOAN_YEARS * 12;
  const monthlyRate = LOAN_ANNUAL_RATE / 12;
  const monthlyPrincipal = months ? loanAmount / months : 0;
  const monthlyInterest = loanAmount * monthlyRate;
  const monthlyPayment = monthlyPrincipal + monthlyInterest;
  const bankSecond = Math.max(onRoadTotal - deposit - loanAmount, 0);
  const chosenColor = color || vehicle.defaultColor || "";
  const gifts = giftPairs(vehicle.gifts);
  const feeRows = [
    { label: tr("Thuế trước bạ (tạm tính)"), amount: money(registrationTax, true), gift: gifts[0] },
    { label: tr("Phí bấm biển số"), amount: money(licensePlate, true), gift: gifts[1] },
    { label: tr("Lệ phí đăng kiểm"), amount: money(inspection, true), gift: gifts[2] },
    { label: tr("Bảo hiểm TNDS + Người ngồi xe (1 năm)"), amount: money(insurance, true), gift: gifts[3] },
    { label: tr("Phí sử dụng đường bộ (1 năm)"), amount: money(roadUse, true), gift: gifts[4] },
  ];

  return (
    <article
      id="quote-sheet"
      className="overflow-hidden rounded-sm border border-[#1f1f1f] bg-white text-[13px] text-[#1f1f1f] shadow-card print:shadow-none"
    >
      <table className="w-full table-fixed border-collapse">
        <colgroup>
          <col className="w-[28%]" />
          <col className="w-[14%]" />
          <col className="w-[8%]" />
          <col className="w-[18%]" />
          <col className="w-[10%]" />
          <col className="w-[12%]" />
          <col className="w-[10%]" />
        </colgroup>
        <tbody>
          <tr>
            <Td colSpan={7} className="bg-white px-3 py-3">
              <div className="flex items-center gap-2">
                <img src="/brand/header-left.png" alt="Mitsubishi" className="h-12 w-auto shrink-0 object-contain object-center" />
                <div className="min-w-0 flex-1 text-center leading-snug">
                  {DEALER_NAME_LINES.map((line) => (
                    <p key={line} className="text-sm font-black uppercase tracking-wide text-[#1f1f1f] sm:text-base">
                      {line}
                    </p>
                  ))}
                  {DEALER_ADDRESS_LINES.map((line, index) => (
                    <p
                      key={line}
                      className={`text-xs font-medium text-[#1f1f1f] sm:text-sm ${index === 0 ? "mt-1" : ""}`}
                    >
                      {line}
                    </p>
                  ))}
                </div>
                <img src="/brand/header-right.png" alt="Moveo New City" className="h-11 w-auto shrink-0 object-contain object-center" />
              </div>
            </Td>
          </tr>
          <tr>
            <Td colSpan={7}>
              <span className="font-semibold">{tr("Khách hàng:")} </span>
              {customerName}
            </Td>
          </tr>
          <tr>
            <Td colSpan={7}>
              <span className="font-semibold">{tr("Địa chỉ:")} </span>
              {customerAddress}
              <span className="ml-8 font-semibold">{tr("TVBH:")}</span>
              <span className="mx-8 font-semibold">- {tr("SĐT:")}</span>
            </Td>
          </tr>
          <tr>
            <Th>{tr("Loại xe:")}</Th>
            <Td className="font-semibold">{vehicle.name}</Td>
            <Td />
            <Td>
              <span className="font-semibold">{tr("Đời xe:")} </span>
              {vehicle.year ?? ""}
            </Td>
            <Td colSpan={3}>
              <span className="font-semibold">{tr("Ngày:")} </span>
              {todayLabel()}
            </Td>
          </tr>
          <tr>
            <Th>{tr("Giá niêm yết:")}</Th>
            <Td className="text-right font-semibold">{money(listPrice)}</Td>
            <Td />
            <Th>{tr("TG giao xe:")}</Th>
            <Td>{vehicle.deliveryNote || ""}</Td>
            <Th>{tr("Màu xe")}</Th>
            <Td>
              <span className="inline-flex items-center gap-2 font-semibold text-[#1f1f1f]">
                <span
                  className="h-4 w-4 shrink-0 rounded-full border border-[#1f1f1f]/40"
                  style={{ background: colorHex(chosenColor) }}
                />
                {chosenColor}
              </span>
            </Td>
          </tr>
          <tr>
            <Th>{tr("Giảm giá:")}</Th>
            <Td className="text-right">{money(discount)}</Td>
            <Td />
            <Td colSpan={4} className="bg-[#fff2cc] font-medium italic">
              {tr(QUOTE_VALIDITY)}
            </Td>
          </tr>
          <tr>
            <Th className="bg-[#fff2cc]">{tr("Giá Bán:")}</Th>
            <Td className="bg-[#fff2cc] text-right text-base font-black">{money(salePrice)}</Td>
            <Td />
            <Td colSpan={4} className="font-medium">
              {tr("ĐVT: VNĐ")}
            </Td>
          </tr>
          <tr>
            <Th colSpan={2} className="bg-[#e60012] text-center text-white">
              {tr("Tạm tính chi phí")}
            </Th>
            <Td />
            <Th colSpan={4} className="bg-[#e60012] text-center text-white">
              {tr("Quà Tặng")}
            </Th>
          </tr>
          {feeRows.map((row) => (
            <tr key={row.label}>
              <Td>{row.label}</Td>
              <Td className="text-right font-medium">{row.amount}</Td>
              <Td />
              <Td colSpan={2}>{row.gift?.[0]}</Td>
              <Td colSpan={2}>{row.gift?.[1]}</Td>
            </tr>
          ))}
          <tr>
            <Td>{tr("Bảo hiểm vật chất thân vỏ xe")}</Td>
            <Td className="text-right font-medium text-[#e60012]">
              {optionalBody ? money(optionalBody) : tr("Tặng")}
            </Td>
            <Td />
            <Td colSpan={2}>{gifts[5]?.[0] ?? ""}</Td>
            <Td colSpan={2}>{gifts[5]?.[1] ?? ""}</Td>
          </tr>
          <tr>
            <Td>{tr("Biển số mica")}</Td>
            <Td className="text-right">{money(mica, true)}</Td>
            <Td />
            <Th colSpan={4} className="bg-[#fff2cc] text-center">
              {tr("CHI PHÍ PHÁT SINH THÊM")}
            </Th>
          </tr>
          <tr>
            <Td>{tr("Phí dịch vụ đăng ký xe")}</Td>
            <Td className="text-right font-medium">{money(registrationService, true)}</Td>
            <Td />
            <Td colSpan={2}>{extraAccessory}</Td>
            <Td colSpan={2}>
              {accessories.slice(1).map((item) => item.name).join("; ")}
            </Td>
          </tr>
          <tr>
            <Th className="bg-[#f4b183]">{tr("Tổng Chi Phí Đăng ký xe")}</Th>
            <Td className="bg-[#f4b183] text-right font-black">{money(registrationTotal)}</Td>
            <Td />
            <Td colSpan={2}>{tr("Hộp đen (nếu có)")}</Td>
            <Td colSpan={2} className="font-semibold text-[#e60012]">
              {tr("Tặng")}
            </Td>
          </tr>
          <tr>
            <Th className="bg-[#e60012] text-white">{tr("TỔNG LĂNG BÁNH")}</Th>
            <Td className="bg-[#e60012] text-right text-lg font-black text-white">{money(onRoadTotal)}</Td>
            <Td />
            <Th className="bg-[#fff2cc]">{tr("TỔNG CP PHÁT SINH")}</Th>
            <Td />
            <Td colSpan={2} className="bg-[#fff2cc] text-right font-semibold">
              {money(extrasTotal)}
            </Td>
          </tr>
          <tr>
            <Th colSpan={2} className="bg-[#e60012] text-center text-white">
              {tr("PHƯƠNG ÁN: MUA TIỀN MẶT")}
            </Th>
            <Td />
            <Th colSpan={4} className="bg-[#e60012] text-center text-white">
              {tr("PHƯƠNG ÁN: MUA VAY NGÂN HÀNG")}
            </Th>
          </tr>
          <tr>
            <Td>{tr("Tiền cọc:")}</Td>
            <Td className="text-right font-semibold">{money(deposit)}</Td>
            <Td />
            <Td colSpan={2}>{tr("Tiền cọc")}</Td>
            <Td colSpan={2} className="text-right">
              {money(deposit)}
            </Td>
          </tr>
          <tr>
            <Td>{tr("Chi Phí Phát sinh thêm (Nếu có)")}</Td>
            <Td className="text-right">{money(extrasTotal)}</Td>
            <Td />
            <Td colSpan={2}>{tr("Số tiền vay")}</Td>
            <Td colSpan={2} className="text-right">
              {money(loanAmount)}
            </Td>
          </tr>
          <tr>
            <Th className="bg-[#fff2cc]">{tr("THANH TOÁN LẦN 2")}</Th>
            <Td className="bg-[#fff2cc] text-right font-black">{money(cashSecond)}</Td>
            <Td />
            <Td colSpan={2}>{tr("Chi Phí Phát sinh thêm (Nếu có)")}</Td>
            <Td colSpan={2} className="text-right">
              {money(extrasTotal)}
            </Td>
          </tr>
          <tr>
            <Td colSpan={3} />
            <Th colSpan={2} className="bg-[#fff2cc]">
              {tr("THANH TOÁN LẦN 2")}
            </Th>
            <Td colSpan={2} className="bg-[#fff2cc] text-right font-black">
              {money(bankSecond)}
            </Td>
          </tr>
          <tr>
            <Th colSpan={2} className="bg-[#e60012] text-center text-white">
              {tr("CÁC MÀU XE")}
            </Th>
            <Td />
            <Th colSpan={4} className="bg-[#e60012] text-center text-white">
              {tr("PHƯƠNG ÁN TRẢ HÀNG THÁNG")}
            </Th>
          </tr>
          <tr>
            <Td colSpan={2} rowSpan={4} className="align-top bg-white">
              <div className="grid grid-cols-2 gap-2 p-1">
                {colors(vehicle).map((name) => (
                  <div
                    key={name}
                    className={`rounded border px-1 py-1 ${
                      name === chosenColor ? "border-[#1f1f1f] bg-[#f7f7f7]" : "border-[#d4d4d4] bg-white"
                    }`}
                  >
                    <img
                      src={colorPhoto(name, vehicle.colorPhotos)}
                      alt={name}
                      className="mx-auto h-12 w-auto object-contain"
                    />
                    <p className={`mt-0.5 text-center text-[11px] ${paintLabelClass(name, name === chosenColor)}`}>
                      {name}
                    </p>
                  </div>
                ))}
              </div>
            </Td>
            <Td />
            <Td>{tr("Thời gian vay:")}</Td>
            <Td>
              {LOAN_YEARS}
              {tr(" Năm")}
            </Td>
            <Td className="text-right">{months}</Td>
            <Td className="text-right">{money(monthlyPrincipal)}</Td>
          </tr>
          <tr>
            <Td />
            <Td>{tr("Tiền gốc tháng:")}</Td>
            <Td>{(LOAN_ANNUAL_RATE * 100).toFixed(1)}%</Td>
            <Td className="text-right">{monthlyRate.toFixed(4)}</Td>
            <Td className="text-right">{money(monthlyInterest)}</Td>
          </tr>
          <tr>
            <Td />
            <Th colSpan={3} className="bg-[#fff2cc]">
              {tr("Thanh toán tháng")}
            </Th>
            <Td className="bg-[#fff2cc] text-right font-black">
              {money(monthlyPayment)}
            </Td>
          </tr>
          <tr>
            <Td />
            <Td colSpan={4} className="text-[11px] leading-relaxed text-[#4b5563]">
              {tr(BANK_NOTE)}
            </Td>
          </tr>
          <tr>
            <Td colSpan={7}>
              {tr(`* Chính sách bảo hành: ${vehicle.warrantyNote || "3 năm/100.000km"} tùy theo điều kiện nào đến trước`)}
            </Td>
          </tr>
          <tr>
            <Td colSpan={2} className="h-28 text-center align-top">
              <p className="font-black uppercase tracking-wide">{tr("XÁC NHẬN TVBH")}</p>
              <p className="mt-10 text-xs text-[#6b7280]">{tr("Ký và ghi rõ họ tên")}</p>
            </Td>
            <Td />
            <Td colSpan={4} className="text-center align-top">
              <p className="font-black uppercase tracking-wide">{tr("XÁC NHẬN KHÁCH HÀNG")}</p>
              <p className="mt-10 text-xs text-[#6b7280]">{tr("Ký và ghi rõ họ tên")}</p>
            </Td>
          </tr>
        </tbody>
      </table>
    </article>
  );
}

function Th({
  children,
  className = "",
  colSpan,
}: {
  children: ReactNode;
  className?: string;
  colSpan?: number;
}) {
  const hasBg = className.includes("bg-");
  const hasText = className.includes("text-");
  return (
    <th
      colSpan={colSpan}
      className={`border border-[#1f1f1f] px-2 py-1.5 text-left font-semibold ${hasBg ? "" : "bg-[#f3f3f3]"} ${hasText ? "" : "text-[#1f1f1f]"} ${className}`}
    >
      {children}
    </th>
  );
}

function Td({
  children,
  className = "",
  colSpan,
  rowSpan,
}: {
  children?: ReactNode;
  className?: string;
  colSpan?: number;
  rowSpan?: number;
}) {
  return (
    <td
      colSpan={colSpan}
      rowSpan={rowSpan}
      className={`border border-[#1f1f1f] px-2 py-1.5 ${className.includes("text-white") ? "" : "text-[#1f1f1f]"} ${className}`}
    >
      {children}
    </td>
  );
}
